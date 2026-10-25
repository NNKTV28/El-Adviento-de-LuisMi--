"""
Picks 25 random Luis Miguel songs and downloads them into the app's resource folder.

- Song list and square cover art come from the public iTunes Search API.
- Audio is fetched with yt-dlp (YouTube search) and transcoded to AAC .m4a with ffmpeg.
- Day 25 is always a track from the "Navidades" album.

Output: composeApp/src/commonMain/composeResources/files/songs/
        dayNN.m4a, dayNN.jpg and songs.json

Usage:  python tools/download_songs.py [--seed 1234]
Personal use only: the downloaded audio is copyrighted.
"""
import argparse
import json
import random
import re
import shutil
import subprocess
import sys
import urllib.parse
import urllib.request
from pathlib import Path

ARTIST_ID = 336904  # Luis Miguel on Apple Music
ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "composeApp" / "src" / "commonMain" / "composeResources" / "files" / "songs"
TMP = ROOT / "tools" / ".download_tmp"


def fetch_json(url: str) -> dict:
    with urllib.request.urlopen(url, timeout=30) as r:
        return json.load(r)


def normalize(title: str) -> str:
    t = title.lower()
    t = re.sub(r"\(.*?\)|\[.*?\]", "", t)
    t = re.sub(r"\s+-\s+.*$", "", t)  # "Song - Remastered 2020"
    return re.sub(r"[^a-z0-9áéíóúñü ]", "", t).strip()


def is_unwanted(track: dict) -> bool:
    name = (track.get("trackName", "") + " " + track.get("collectionName", "")).lower()
    return any(w in name for w in ("live", "en vivo", "directo", "karaoke", "instrumental", "remix", "medley", "popurr"))


def catalog() -> list[dict]:
    tracks = []
    for country in ("es", "mx", "us"):
        data = fetch_json(f"https://itunes.apple.com/lookup?id={ARTIST_ID}&entity=song&limit=200&country={country}")
        tracks += [r for r in data["results"] if r.get("wrapperType") == "track" and r.get("artistName") == "Luis Miguel"]
        data = fetch_json(
            "https://itunes.apple.com/search?" + urllib.parse.urlencode(
                {"term": "luis miguel", "entity": "song", "attribute": "artistTerm", "limit": 200, "country": country}))
        tracks += [r for r in data["results"] if r.get("artistName") == "Luis Miguel"]
    return tracks


def download_audio(query: str, target: Path) -> bool:
    TMP.mkdir(parents=True, exist_ok=True)
    for f in TMP.glob("*"):
        f.unlink()
    cmd = [
        "yt-dlp", f"ytsearch5:{query}",
        "--match-filter", "duration > 90 & duration < 540 & !is_live",
        "--max-downloads", "1",
        "-f", "bestaudio", "--no-playlist", "--quiet", "--no-warnings",
        "-o", str(TMP / "audio.%(ext)s"),
    ]
    subprocess.run(cmd, check=False)
    files = list(TMP.glob("audio.*"))
    if not files:
        return False
    # Transcode to AAC so both AVAudioPlayer (iOS) and MediaPlayer (Android) can play it.
    r = subprocess.run(
        ["ffmpeg", "-y", "-loglevel", "error", "-i", str(files[0]), "-vn", "-c:a", "aac", "-b:a", "160k",
         "-movflags", "+faststart", str(target)])
    return r.returncode == 0 and target.exists()


def download_cover(track: dict, target: Path):
    url = track["artworkUrl100"].replace("100x100bb", "600x600bb")
    with urllib.request.urlopen(url, timeout=30) as r, open(target, "wb") as f:
        shutil.copyfileobj(r, f)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--seed", type=int, default=None)
    args = ap.parse_args()
    rng = random.Random(args.seed)

    unique: dict[str, dict] = {}
    for t in catalog():
        if is_unwanted(t) or "artworkUrl100" not in t:
            continue
        unique.setdefault(normalize(t["trackName"]), t)

    christmas = [t for t in unique.values() if "navidad" in t.get("collectionName", "").lower()]
    regular = [t for t in unique.values() if t not in christmas]
    print(f"catalog: {len(regular)} regular songs, {len(christmas)} christmas songs")
    rng.shuffle(regular)
    rng.shuffle(christmas)

    OUT.mkdir(parents=True, exist_ok=True)
    songs = []
    pools = {day: (christmas if day == 25 else regular) for day in range(1, 26)}
    for day in range(1, 26):
        pool = pools[day]
        while pool:
            t = pool.pop()
            name = f"day{day:02d}"
            query = f"Luis Miguel {t['trackName']} audio oficial"
            print(f"[{day:02d}] {t['trackName']} ({t['collectionName']})", flush=True)
            if not download_audio(query, OUT / f"{name}.m4a"):
                print("     audio failed, picking another song")
                continue
            download_cover(t, OUT / f"{name}.jpg")
            songs.append({
                "day": day,
                "title": re.sub(r"\s*\((Remasterizado|Remastered).*?\)", "", t["trackName"]),
                "album": t["collectionName"],
                "year": int(t.get("releaseDate", "0000")[:4]),
                "audio": f"files/songs/{name}.m4a",
                "cover": f"files/songs/{name}.jpg",
            })
            break
        else:
            sys.exit(f"ran out of songs for day {day}")

    (OUT / "songs.json").write_text(json.dumps(songs, ensure_ascii=False, indent=2), encoding="utf-8")
    shutil.rmtree(TMP, ignore_errors=True)
    print(f"done: {len(songs)} songs in {OUT}")


if __name__ == "__main__":
    main()
