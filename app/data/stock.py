import yfinance as yf

tickers = ["TQQQ", "UPRO", "SOXL"]
for t in tickers:
    df = yf.download(t, start="2020-01-01", end="2025-05-15")
    df.to_csv(f"{t}.csv")
