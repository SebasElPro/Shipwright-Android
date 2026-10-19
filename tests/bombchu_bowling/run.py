"""Test the production ammo registration function, including title-screen startup.

Run from the repository root: python3 tests/bombchu_bowling/run.py
"""
from pathlib import Path
import subprocess
import tempfile

root = Path(__file__).resolve().parents[2]
source = (root / "soh/soh/Enhancements/Minigames/BombchuBowling.cpp").read_text()
function = source.split("static void RegisterBombchuBowlingAmmo() {", 1)[1]
function = "static void RegisterBombchuBowlingAmmo() {" + function.split(
    "static void RegisterBombchuBowlingFirstPrize()", 1
)[0]
fixture = (Path(__file__).parent / "harness.cpp.in").read_text().replace("@FUNCTION@", function)
with tempfile.TemporaryDirectory(prefix="soh-bowling-test-") as directory:
    cpp = Path(directory) / "test.cpp"
    binary = Path(directory) / "test"
    cpp.write_text(fixture)
    subprocess.run(["c++", "-std=c++20", "-fsanitize=address,undefined", str(cpp), "-o", str(binary)], check=True)
    subprocess.run([str(binary)], check=True)
print("Bombchu Bowling startup and live ammo regression checks passed")
