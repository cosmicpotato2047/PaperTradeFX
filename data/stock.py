import yfinance as yf

tickers = ["TQQQ", "UPRO", "SOXL"]
for t in tickers:
    df = yf.download(t, start="2025-01-19", end="2025-05-16")
    df.to_csv(f"{t}.csv")
