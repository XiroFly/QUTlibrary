import requests
from Crypto.PublicKey import RSA
from Crypto.Cipher import PKCS1_OAEP
from Crypto.Cipher import PKCS1_v1_5
import base64
from datetime import datetime
import json
def save_needlist(needlist):
    with open('needlist.json', 'w', encoding='utf-8') as f:
        json.dump(needlist, f, ensure_ascii=False, indent=4)

def load_needlist():
    try:
        with open('needlist.json', 'r', encoding='utf-8') as f:
            return json.load(f)
    except FileNotFoundError:
        return []
session = requests.Session()
needlist=[]#依次为token resvId uuid devid（devSn）
# 获取 publicKey 和 nonceStr,然后登录
def signin():
    public_key_url = "http://10.20.15.27/ic-web/login/publicKey"
    login_headers = {
        "Host": "10.20.15.27",
        "Proxy-Connection": "keep-alive",
        "Accept": "application/json, text/plain, */*",
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
        "lan": "1",
        "Origin": "http://10.20.15.27",
        "X-Requested-With": "com.tencent.mm",
        "Referer": "http://10.20.15.27/mobile.html",
        "Accept-Encoding": "gzip, deflate",
        "Accept-Language": "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7",
    }
    response = session.get(public_key_url, headers=login_headers)
    public_key_data = response.json()['data']

    # 提取 publicKey 和 nonceStr
    public_key_str = f"-----BEGIN PUBLIC KEY-----\n{public_key_data['publicKey']}\n-----END PUBLIC KEY-----"
    # public_key_str ="""-----BEGIN PUBLIC KEY-----
    # MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQChuIdDu7MzSWp+J24eYSguh7ut
    # zpEh8yL5OMs4RORRdgFBa9A7WBJxNkT+ZR7GJBa3P1X1o3KJGJeDHwlCcS292b34
    # 7Z1lcc24czh0vH7+dY7R0oEvXA+Ez4/qFOdmT5p76+BKkFrnQuzhdTV6nlzfSh3Q
    # gvLmSyrZiFJ8PH3QfQIDAQAB
    # -----END PUBLIC KEY-----"""
    nonce_str = public_key_data['nonceStr']
    print(public_key_str,nonce_str)
    # 加密信息
    图书馆密码="*****"
    message = f"{图书馆密码};{nonce_str}".encode('GBK')

    # 解析公钥
    public_key = RSA.import_key(public_key_str)

    # 创建RSA加密器
    cipher_rsa = PKCS1_v1_5.new(public_key)

    # 加密消息
    encrypted_message = cipher_rsa.encrypt(message)
    encrypted_message_hex = encrypted_message.hex()
    # 确保结果长度与前端一致
    if len(encrypted_message_hex) < 256:
        encrypted_message_hex = '0' * (256 - len(encrypted_message_hex)) + encrypted_message_hex
    encrypted_message_bytes = bytes.fromhex(encrypted_message_hex)

    # 将字节对象转换为Base64编码的字符串
    encoded_encrypted_message = base64.b64encode(encrypted_message_bytes).decode('GBK')
    #encoded_encrypted_message="FpdFEAJmg8UVM40lZuxJjDbtINLpvQ+oOTrB8dYbXtgWkLx4ctNBUMluDRYNHLEVN+RGVEyfgApZ3j/zkvk322GCv4as+U57byPA3DZosq/YaIy61Cthi7fQLFPiZwnHY+x6Kh1++AbKdCHPZGe5XaqJhplDjKovhtH/lFHfGuI="
    # print(encoded_encrypted_message)

    # 设置登录URL
    login_url = "http://10.20.15.27/ic-web/login/user"

    # 设置请求体
    login_payload = {
        "logonName": "******************************",#学号
        "password": encoded_encrypted_message,
        "captcha": "",
        "consoleType": 16
    }

    # 设置请求头
    login_headers = {
        "Host": "10.20.15.27",
        "Proxy-Connection": "keep-alive",
        "Accept": "application/json, text/plain, */*",
        "Content-Type": "application/json;charset=UTF-8",
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
        "lan": "1",
        "Origin": "http://10.20.15.27",
        "X-Requested-With": "com.tencent.mm",
        "Referer": "http://10.20.15.27/mobile.html",
        "Accept-Encoding": "gzip, deflate",
        "Accept-Language": "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7",
        "Content-Length": str(len(str(login_payload))),
    }

    # 发送POST请求
    response = session.post(login_url, json=login_payload, headers=login_headers)
    login_response_data = response.json()
    needlist.append(login_response_data['data']['token'])
    print(f"token:{needlist}")
    return response
    

def get_next_timesection():
    current_time = datetime.now().time()  # 获取当前时间
    timesections = [("09:00:00", "12:00:00"), ("13:30:00", "18:00:00"), ("19:00:00", "22:00:00")]

    # 将时间段字符串转换为时间对象
    time_sections = [(datetime.strptime(start, "%H:%M:%S").time(), datetime.strptime(end, "%H:%M:%S").time()) for start, end in timesections]

    # 查找当前时间所在的时间段
    for i, (start, end) in enumerate(time_sections):
        if start <= current_time <= end:
            # 如果在某个时间段内，返回下一个时间段（如果存在）
            return timesections[i + 1] if i + 1 < len(timesections) else None

    # 如果不在任何时间段，选择下一个相邻的时间段
    for start, end in time_sections:
        if current_time < start:
            return (start.strftime("%H:%M:%S"), end.strftime("%H:%M:%S"))

    # 如果当前时间在最后一个时间段之后，返回 None
    return None
# 接下来实现预约
def reserve():
    response=signin()
    # 预约URL
    reserve_url = "http://10.20.15.27/ic-web/reserve"
    # 获取当前日期
    current_date = datetime.now().date()
    # timesections = [("07:00:00", "12:00:00"), ("13:30:00", "18:00:00"), ("19:00:00", "22:00:00")]
    timesections=get_next_timesection()
    
    # 设置预约请求体
    res_payload = {
        "testName": "",
        "appAccNo": 135820,
        "memberKind": 1,
        "resvDev": [100495137],
        "resvMember": [135820],
        "resvProperty": 0,
        "sysKind": 8,
        "resvBeginTime": f"{current_date} {timesections[0]}",
        "resvEndTime": f"{current_date} {timesections[1]}"
    }
    # 设置预约请求头
    reserve_headers = {
        "Host": "10.20.15.27",
        "Proxy-Connection": "keep-alive",
        "Accept": "application/json, text/plain, */*",
        "Content-Type": "application/json;charset=UTF-8",
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
        "token": needlist[0],
        "lan": "1",
        "Origin": "http://10.20.15.27",
        "X-Requested-With": "com.tencent.mm",
        "Referer": "http://10.20.15.27/mobile.html",
        "Accept-Encoding": "gzip, deflate",
        "Accept-Language": "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7",
    }
    # 发送预约请求
    response = session.post(reserve_url, json=res_payload, headers=reserve_headers)
    print("预约响应内容:", response.headers, response.text, end="\n")
    needlist.append(response.json()['data']['resvId'])
    needlist.append(response.json()['data']['uuid'])
    needlist.append(response.json()['data']['resvDevInfoList'][0]['devId'])
    save_needlist(needlist)
    return response


#实现取消预约
def deleteReserve():
    signin()
    needlist=load_needlist()
    #取消预约逻辑
    url = 'http://10.20.15.27/ic-web/reserve/delete'
    headers = {
        "Host": "10.20.15.27",
        "Accept": "application/json, text/plain, */*",
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36",
        "token": needlist[0],
        "Content-Type": "application/json;charset=UTF-8",
        "Origin": "http://10.20.15.27",
        'Referer': 'http://10.20.15.27/',
        "Accept-Encoding": "gzip, deflate",
        "Accept-Language": "zh-CN,zh;q=0.9",
    }
    body = {
            'uuid': needlist[2]
        }
    # 发送POST请求
    response = session.post(url, headers=headers, json=body)
    # 处理响应
    if response.status_code == 200:
        print( response.json())
if __name__ == "__main__":
            deleteReserve()     
           # reserve()
