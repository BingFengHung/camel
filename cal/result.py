import csv
import json
import os
import re

file_path = 'result.txt'


def parse_string(input_string):
    # 使用正規表示式拆解字串
    # match = re.match(r"([a-zA-Z]+)_t(\d)(\d+)\.txt", input_string)
    # match = re.match(r"([a-zA-Z]+)_t(\d)(\d+)(?:\.txt)?", input_string)
    match = re.match(r"([a-zA-Z]+)_t(\d+)d(\d+)(?:\.txt)?", input_string)

    if match:
        # 依序取出符合的部分
        part1 = match.group(1)  # 英文字母部分
        part2 = match.group(2)  # 併發數字部分
        part3 = match.group(3)  # 參數數字部分
        return part1, int(part2), int(part3)
    else:
        raise ValueError(f"無法解析字串: {input_string}`")
    
dicts = {}
lines = []
def process_content(text):
    part = text.split(": ")
    value = float(part[1].strip())
    items = part[0]
    
    it = parse_string(items)

    # if it[0] not in dicts:
    #     dicts[it[0]] = []
    
    # dicts[it[0]].append({
    #     "concurrency": it[1],
    #     "param": it[2],
    #     "time": value
    # })
    if it is not None: 
        lines.append([it[0], it[1], it[2], value])


if __name__ == '__main__':
    with open(file_path, 'r') as file:
        for line in file: 
            process_content(line)

    formatted_data = json.dumps(dicts, indent=4)
    # print(formatted_data)
    # print(lines)
    # 指定要寫入的 CSV 檔案名稱
    filename = 'output.csv'

    # 開啟檔案，並使用 csv.writer 來寫入數據
    with open(filename, 'w', newline='', encoding='utf-8') as file:
        writer = csv.writer(file)
        writer.writerows(lines)

    print('CSV 檔案已保存。')