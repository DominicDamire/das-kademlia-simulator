"""Draw docs/results.png from the console output of one simulator run.

    python docs/plot_results.py docs/sample-run-500.log docs/results.png

Needs matplotlib. Reads three kinds of lines from the run output:
  "[PUT] ... Full operation took N time units"   -> PUT time (simulated ms)
  "GET operation took N time units."             -> one value per GET
  "[time=...] ... [D=M msg deliv] ..."           -> messages sent since the last
      observer reset; the first such line after a PUT/GET is that operation's count
      (the observer resets its counters at every GET, see README "Limits").
"""
import re
import sys
from collections import Counter

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.ticker import FuncFormatter

log_path, out_path = sys.argv[1], sys.argv[2]

put_ms, put_msgs, get_ms, get_msgs = None, None, [], []
waiting = None  # which operation the next observer line belongs to
for line in open(log_path, encoding="utf-8"):
    if m := re.match(r"\[PUT\].*Full operation took (\d+) time units", line):
        put_ms, waiting = int(m[1]), "put"
    elif m := re.match(r"GET operation took (\d+) time units", line):
        get_ms.append(int(m[1]))
        waiting = "get"
    elif waiting and (m := re.match(r"\[time=\d+\].*\[D=([\d.]+) msg deliv\]", line)):
        if waiting == "put":
            put_msgs = float(m[1])
        else:
            get_msgs.append(float(m[1]))
        waiting = None
assert put_ms and put_msgs and get_ms and len(get_ms) == len(get_msgs), "unexpected log format"

n = len(get_ms)
get_sorted = sorted(get_ms)
median = (get_sorted[(n - 1) // 2] + get_sorted[n // 2]) / 2
mean_msgs = sum(get_msgs) / n

# colours: validated default palette (blue = GET, neutral grey = PUT context), light surface
SURFACE, INK, INK2, MUTED, GRID, AXIS = "#fcfcfb", "#0b0b0b", "#52514e", "#898781", "#e1e0d9", "#c3c2b7"
BLUE, GREY = "#2a78d6", "#898781"
plt.rcParams.update({"font.family": "sans-serif",
                     "font.sans-serif": ["Helvetica Neue", "Helvetica", "Arial", "DejaVu Sans"],
                     "text.color": INK, "axes.labelcolor": INK2, "xtick.color": MUTED, "ytick.color": MUTED})

fig, (a, b) = plt.subplots(1, 2, figsize=(8.0, 3.55), dpi=200, facecolor=SURFACE,
                           gridspec_kw={"width_ratios": [1, 1.1], "wspace": 0.42,
                                        "left": 0.07, "right": 0.97, "top": 0.77, "bottom": 0.29})
for ax in (a, b):
    ax.set_facecolor(SURFACE)
    for side in ("top", "right", "left"):
        ax.spines[side].set_visible(False)
    ax.spines["bottom"].set_color(AXIS)
    ax.spines["bottom"].set_linewidth(0.6)
    ax.tick_params(length=0, labelsize=9, pad=6)
    ax.set_axisbelow(True)

# (a) dot plot: one dot per GET, stacked by value
counts = Counter(get_ms)
xs = [v for v, c in sorted(counts.items()) for _ in range(c)]
ys = [i + 1 for v, c in sorted(counts.items()) for i in range(c)]
a.scatter(xs, ys, s=95, color=BLUE, edgecolor=SURFACE, linewidth=1.6, zorder=3)
pad = 40
a.set_xlim(min(get_ms) - pad, max(get_ms) + pad)
a.set_ylim(0, max(counts.values()) + 0.8)
a.set_xticks(range(min(get_ms) // 20 * 20, max(get_ms) + 1, 20))
a.set_yticks(range(0, max(counts.values()) + 1, 2))
a.yaxis.grid(True, color=GRID, linewidth=0.5)
a.set_xlabel("simulated ms", fontsize=9, labelpad=6)
a.set_ylabel("number of GETs", fontsize=9, labelpad=8)
a.set_title("GET: time to collect 1,023 chunks", loc="left", fontsize=10.5, fontweight="bold", pad=24, x=-0.07)
a.text(-0.07, 1.04, f"{n} GETs: min {min(get_ms)}, median {median:g}, max {max(get_ms)} ms",
       transform=a.transAxes, fontsize=9, color=INK2)

# (b) bars: messages sent by the PUT vs by one GET
b.barh([1, 0], [put_msgs, mean_msgs], height=0.42, color=[GREY, BLUE], zorder=3)
b.set_yticks([1, 0], ["PUT\n(25 MiB)", f"GET\n(average of {n})"], fontsize=9, color=INK2)
b.set_xlim(0, 4.0e6)
b.set_xticks([0, 1e6, 2e6, 3e6, 4e6])
b.set_ylim(-0.6, 1.6)
b.xaxis.set_major_formatter(FuncFormatter(lambda x, _: "0" if x == 0 else f"{x / 1e6:g} M"))
b.xaxis.grid(True, color=GRID, linewidth=0.5)
b.set_xlabel("messages sent (ACKs not counted)", fontsize=9, labelpad=6)
b.text(put_msgs + 6e4, 1, f"{put_msgs / 1e6:.2f} M", va="center", fontsize=9.5, color=INK, fontweight="bold")
b.text(put_msgs + 6e4, 0.78, f"PUT took {put_ms} ms", va="center", fontsize=8.5, color=INK2)
b.text(mean_msgs + 6e4, 0, f"{mean_msgs / 1e3:.1f} k  (range {min(get_msgs) / 1e3:.1f}-{max(get_msgs) / 1e3:.1f} k)",
       va="center", fontsize=9.5, color=INK, fontweight="bold")
b.set_title("Messages per operation", loc="left", fontsize=10.5, fontweight="bold", pad=24, x=-0.30)
b.text(-0.30, 1.04, f"one PUT and the average of {n} GETs", transform=b.transAxes, fontsize=9, color=INK2)

fig.text(0.045, 0.04, "500 nodes, one simulated hour (1 PUT of 25,600 chunks x 36 replicas, then 11 GETs of 1,023 random chunks). "
         "Source: docs/sample-run-500.log", fontsize=7, color=MUTED)
fig.savefig(out_path, facecolor=SURFACE)
print(f"PUT {put_ms} ms / {put_msgs:.0f} msgs; GET ms {get_ms}; GET msgs {[int(x) for x in get_msgs]}")
