import csv
import random
import time

# 設定檔案名稱
file_name = "influxdb_test_data.csv"

# 建立 CSV 檔案並寫入標題
with open(file_name, mode='w', newline='') as csvfile:
    csvwriter = csv.writer(csvfile)
    
    # 寫入標題行
    header = ["timestamp", "file_name"] + [f"channel_{i+1}" for i in range(80)]
    csvwriter.writerow(header)

    # 生成 500,000 筆資料
    for i in range(500000):
        # 時間戳，這裡使用當前時間加上筆記的索引以模擬連續時間
        timestamp = int(time.time_ns()) + i * 15  # 每15ns一筆記錄
        channel_data = [round(random.uniform(0, 100), 6) for _ in range(80)]  # 隨機生成 80 個 channel 數據
        
        # 寫入一行數據
        row = [timestamp, f"file_{i//10000}"] + channel_data  # 將每 10,000 筆資料的檔名組織成 'file_0', 'file_1', ...
        csvwriter.writerow(row)

print(f"CSV 文件 {file_name} 已生成，包含 500,000 筆數據。")