import pandas as pd
import numpy as np
from pathlib import Path

# 처리할 CSV 파일 리스트
files = ["SOXL.csv", "TQQQ.csv", "UPRO.csv"]

for fname in files:
    input_path = Path(fname)
    if not input_path.exists():
        print(f"파일을 찾을 수 없음: {input_path}")
        continue

    # 1) 1~2행(헤더 메타데이터)을 건너뛰고, 직접 컬럼명을 지정
    df = pd.read_csv(
        input_path,
        skiprows=2,        # 첫 번째 Price/Close... 행과 Ticker 행을 건너뜀
        header=None,
        names=["Date", "Close", "High", "Low", "Open", "Volume"]
    )

    # 2) 메타데이터용 'Date' 행(첫 줄)을 제거
    df = df[df["Date"] != "Date"].copy()

    # 3) 가격 컬럼 절삭(truncate): 소수점 둘째 자리까지만
    for col in [ "Close", "High", "Low", "Open"]:
        df[col] = pd.to_numeric(df[col], errors="coerce")
        df[col] = np.floor(df[col] * 100) / 100

    # 4) 결과 저장
    output_path = input_path.with_name(input_path.stem + "_trunc.csv")
    df.to_csv(output_path, index=False, encoding="utf-8-sig")
    print(f"Saved -> {output_path}")

print("모든 파일 처리 완료!")
