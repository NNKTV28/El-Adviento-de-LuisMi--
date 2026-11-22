"""
Downloads the 25 advent songs into the app's resource folder.

Two modes:
- Random (default): picks 25 random Luis Miguel songs; day 25 is always from the "Navidades" album.
  Song list from the public iTunes Search API, audio found with a YouTube search.
- Playlist (--playlist URL): takes exactly the 25 videos of a YouTube / YouTube Music playlist,
  in order (video 1 = day 1). Title, album and year come from the video's metadata or iTunes.

Cover art is the 600x600 iTunes artwork; audio is transcoded to AAC .m4a with ffmpeg.

Output: composeApp/src/commonMain/composeResources/files/songs/
        dayNN.m4a, dayNN.jpg and songs.json

Usage:  python tools/download_songs.py [--seed 1234]
        python tools/download_songs.py --playlist "https://music.youtube.com/playlist?list=..."
Personal use only: the downloaded audio is copyrighted.
"""
import argparse
import json
import random
import re
import shutil
import subprocess
import sys
import unicodedata
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
    t = "".join(c for c in unicodedata.normalize("NFKD", t) if not unicodedata.combining(c))  # "llegó" == "llego"
    return re.sub(r"[^a-z0-9 ]", "", t).strip()


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
    return bool(files) and transcode(files[0], target)


def download_cover(track: dict, target: Path):
    url = track["artworkUrl100"].replace("100x100bb", "600x600bb")
    with urllib.request.urlopen(url, timeout=30) as r, open(target, "wb") as f:
        shutil.copyfileobj(r, f)


def transcode(source: Path, target: Path) -> bool:
    # AAC so both AVAudioPlayer (iOS) and MediaPlayer (Android) can play it.
    r = subprocess.run(
        ["ffmpeg", "-y", "-loglevel", "error", "-i", str(source), "-vn", "-c:a", "aac", "-b:a", "160k",
         "-movflags", "+faststart", str(target)])
    return r.returncode == 0 and target.exists()


def video_info(url: str, flat: bool = False) -> dict:
    cmd = ["yt-dlp", "-J", "--skip-download", "--no-warnings"] + (["--flat-playlist"] if flat else []) + [url]
    return json.loads(subprocess.run(cmd, check=True, capture_output=True, text=True, encoding="utf-8").stdout)


def clean_title(title: str) -> str:
    t = re.sub(r"^Luis Miguel\s*-\s*", "", title)
    t = re.sub(r"\s*\((Official|Video|Lyric|Audio|Vivo|Remaster)[^)]*\)", "", t, flags=re.I)
    return t.strip()


def itunes_match(title: str, album: str | None) -> dict | None:
    """Best iTunes track for a title (and album, when the video knows it)."""
    for country in ("es", "mx", "us"):
        data = fetch_json("https://itunes.apple.com/search?" + urllib.parse.urlencode(
            {"term": f"Luis Miguel {title}", "entity": "song", "limit": 50, "country": country}))
        tracks = [r for r in data["results"] if r.get("artistName") == "Luis Miguel" and "artworkUrl100" in r
                  and not re.search(r"collection|2 in 1", r.get("collectionName", ""), re.I)]
        same_title = [r for r in tracks if normalize(r["trackName"]) == normalize(title)]
        if album:
            for r in same_title:
                if normalize(r["collectionName"]) == normalize(album):
                    return r
            for r in tracks:
                if normalize(r["collectionName"]) == normalize(album):
                    return r
        studio = [r for r in same_title if not is_unwanted(r)]
        if studio or same_title:
            return (studio or same_title)[0]
    return None


def from_playlist(url: str):
    entries = video_info(url, flat=True)["entries"]
    if len(entries) != 25:
        sys.exit(f"the playlist has {len(entries)} videos, it needs exactly 25")
    OUT.mkdir(parents=True, exist_ok=True)
    songs = []
    for day, entry in enumerate(entries, 1):
        name = f"day{day:02d}"
        video = f"https://www.youtube.com/watch?v={entry['id']}"
        info = video_info(video)
        title = info.get("track") or clean_title(info["title"])
        match = itunes_match(title, info.get("album"))
        album = info.get("album") or (match or {}).get("collectionName") or ""
        year = info.get("release_year") or int((match or {}).get("releaseDate", "0000")[:4])
        print(f"[{day:02d}] {title} ({album}, {year})", flush=True)

        TMP.mkdir(parents=True, exist_ok=True)
        for f in TMP.glob("*"):
            f.unlink()
        subprocess.run(["yt-dlp", video, "-f", "bestaudio", "--no-playlist", "--quiet", "--no-warnings",
                        "-o", str(TMP / "audio.%(ext)s")], check=True)
        if not transcode(next(TMP.glob("audio.*")), OUT / f"{name}.m4a"):
            sys.exit(f"could not convert the audio for day {day}")

        if match:
            download_cover(match, OUT / f"{name}.jpg")
        else:
            # No iTunes match: square crop of the video thumbnail
            print("     no iTunes cover, using the video thumbnail")
            subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", info["thumbnail"], "-vf",
                            "crop=min(iw\\,ih):min(iw\\,ih),scale=600:600", str(OUT / f"{name}.jpg")], check=True)

        songs.append({
            "day": day,
            "title": re.sub(r"\s*\((Remasterizado|Remastered).*?\)", "", title),
            "album": album,
            "year": year,
            "audio": f"files/songs/{name}.m4a",
            "cover": f"files/songs/{name}.jpg",
        })

    (OUT / "songs.json").write_text(json.dumps(songs, ensure_ascii=False, indent=2), encoding="utf-8")
    shutil.rmtree(TMP, ignore_errors=True)
    print(f"done: {len(songs)} songs in {OUT}")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--seed", type=int, default=None)
    ap.add_argument("--playlist", help="YouTube / YouTube Music playlist with exactly 25 videos, in day order")
    args = ap.parse_args()
    if args.playlist:
        from_playlist(args.playlist)
        return
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
