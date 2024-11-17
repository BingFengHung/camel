import os

def process_files_in_directory(directory_path):
    # 確保資料夾存在
    if not os.path.exists(directory_path):
        print(f"資料夾 '{directory_path}' 不存在！")
        return
    
    # 遍歷資料夾中的所有檔案
    for file_name in os.listdir(directory_path):
        file_path = os.path.join(directory_path, file_name)
        
        # 確保是檔案（避免處理資料夾）
        if os.path.isfile(file_path):
            try:
                # 讀取檔案並計算數值平均
                with open(file_path, 'r') as file:
                    numbers = []
                    for line in file:
                        # 去掉空白或換行，檢查是否為空行
                        clean_line = line.strip()
                        if clean_line:  # 空行直接跳過
                            try:
                                numbers.append(float(clean_line))
                            except ValueError:
                                # 無法轉換成數值的行忽略
                                continue
                    if numbers:
                        average = sum(numbers) / len(numbers)
                        print(f"{file_name}: {average}")
                        write_to_output_file('result.txt', f"{file_name}: {average}")
                    else:
                        print(f"{file_name}: 檔案沒有有效數值")
            except Exception as e:
                print(f"讀取檔案 {file_name} 時發生錯誤: {e}")

def write_to_output_file(output_file, content):
    """
    將內容寫入指定的檔案
    """
    try:
        with open(output_file, 'a') as output:  # 使用 'a' 模式進行追加
            output.write(content + '\n')
    except Exception as e:
        print(f"寫入檔案時發生錯誤: {e}")

# 設定目標資料夾路徑
directory = "../test_data"
process_files_in_directory(directory)