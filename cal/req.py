import requests
import time

def request_performance_data(base_url, threads_list, counts_list):
    for thread in threads_list:
        for count in counts_list:
            for i in range(10):
              # 動態生成 URL
              url = f"{base_url}/performance/{thread}/{count}"
              try:
                  # 發送 GET 請求
                  response = requests.get(url)
                  # 檢查回應狀態碼
                  if response.status_code == 200:
                      print(f"成功取得資料 (thread={thread}, count={count}): {response.json()}")
                  else:
                      print(f"請求失敗 (thread={thread}, count={count}): 狀態碼 {response.status_code}")
              except requests.RequestException as e:
                  print(f"請求時發生錯誤: {e}")
              finally:
                  # 間隔 500 毫秒
                  time.sleep(0.3)

# 設定基底 URL
base_url = "http://localhost:8080"

# 設定 threads 和 counts 的值範圍
threads_list = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160]  # 替換成你需要的 threads 值
counts_list = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100]  # 替換成你需要的 counts 值

# 發送請求
request_performance_data(base_url, threads_list, counts_list)