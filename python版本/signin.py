#这里实现扫码签到签退
#签到我拿的是web的cookie，但其实抓包看到有移动端登录接口，实现起来还方便POST http://10.20.15.27/ic-web/phoneSeatReserve/login HTTP/1.1
#移动端和电脑端用的同一个session库诶
import requests
import reserve
import sys
# 创建一个会话对象
session = reserve.session
needlist=reserve.load_needlist()#预约后存的用来签到的reserveId等
#你扫码后显示的页面，要点一下签到的，没用的实现
#手机端登录,要重新登录给服务器unionId
def signin():
    url = "http://10.20.15.27/ic-web/phoneSeatReserve/login"
    headers = {
    "Host": "10.20.15.27",
    "Proxy-Connection": "keep-alive",
    "Content-Length": "82",
    "Accept": "application/json, text/plain, */*",
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
    "Content-Type": "application/json;charset=UTF-8",
    "Origin": "http://10.20.15.27",
    "Referer": "http://10.20.15.27/scancode.html",
    "Accept-Encoding": "gzip, deflate",
    "Accept-Language": "zh-CN,zh;q=0.9",
    }
    payload = {
    "devSn":   needlist[3],
    "unionId": "**************************",#微信unionID
    "type": "1",
    "bind": 0
    }
    response = session.post(url, headers=headers, json=payload)
    print(response.text)
    needlist[0]=response.json().get('data', {}).get('token', None)
    return response
    
def scanInPage():
    reserve.signin()
    # 设置必要的参数
    sta = "1"
    sysid = "1LK"
    lab = "100495496"
    type_ = "1"  # `type` 是 Python 的保留关键字，变量名改为 `type_`
    dev = "100495496"
    unionId = "***********************"#微信unionID
    resvId = "102752916"
    # 生成签到的URL
    signin_url = f"http://10.20.15.27/scancode.html#/login?sta={sta}&sysid={sysid}&lab={lab}&type={type_}&dev={dev}&unionId={unionId}"
    # 打印生成的 URL
    print("签到URL:", signin_url)
    headers = {
        "Host": "10.20.15.27",
        "Accept": "application/json, text/plain, */*",
        "User-Agent": "Mozilla/5.0 (Linux; Android 12; M2102K1AC Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/95.0.4638.74 Safari/537.36 MMWEBID/5908 MicroMessenger/8.0.50.2701(0x28003259) WeChat/arm64 Weixin Android Tablet NetType/WIFI Language/zh_CN ABI/arm64",
        "token": reserve.needlist[0],
        "Content-Type": "application/json;charset=UTF-8",
        "Origin": "http://10.20.15.27",
        "Referer": "http://10.20.15.27/scancode.html",
        "Accept-Encoding": "gzip, deflate",
        "Accept-Language": "zh-CN,zh;q=0.9",
    }
    response= session.get(signin_url,headers=headers)
    print(response.text)
    scanOut()
    return response
#签到
def scanIn():
    signin()
    #签到
    url = "http://10.20.15.27/ic-web/phoneSeatReserve/sign"
    headers = {
        "Host": "10.20.15.27",
        "Accept": "application/json, text/plain, */*",
        "User-Agent": "Mozilla/5.0 (Linux; Android 12; M2102K1AC Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/95.0.4638.74 Safari/537.36 MMWEBID/5908 MicroMessenger/8.0.50.2701(0x28003259) WeChat/arm64 Weixin Android Tablet NetType/WIFI Language/zh_CN ABI/arm64",
        "token": needlist[0],
        "Content-Type": "application/json;charset=UTF-8",
        "Origin": "http://10.20.15.27",
        "Referer": "http://10.20.15.27/scancode.html",
        "Accept-Encoding": "gzip, deflate",
        "Accept-Language": "zh-CN,zh;q=0.9",
    }
    payload = {
        "resvId":needlist[1] #102746399
    }
    # 发送POST请求
    response = session.post(url, headers=headers, json=payload)
    print(response.text)
    return response
    
    
  #签退   
def  scanOut():
    signin()
    #签退
    url = "http://10.20.15.27/ic-web/phoneSeatReserve/quit"
    headers = {
        "Host": "10.20.15.27",
        "Accept": "application/json, text/plain, */*",
        "User-Agent": "Mozilla/5.0 (Linux; Android 12; M2102K1AC Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/95.0.4638.74 Safari/537.36 MMWEBID/5908 MicroMessenger/8.0.50.2701(0x28003259) WeChat/arm64 Weixin Android Tablet NetType/WIFI Language/zh_CN ABI/arm64",
        "token": needlist[0],
        "Content-Type": "application/json;charset=UTF-8",
        "Origin": "http://10.20.15.27",
        "Referer": "http://10.20.15.27/scancode.html",
        "Accept-Encoding": "gzip, deflate",
        "Accept-Language": "zh-CN,zh;q=0.9",
    }
    payload = {
        "resvId":needlist[1] #102746399
    }
    # 发送POST请求
    response = session.post(url, headers=headers, json=payload)

    # 处理响应
    if response.status_code == 200:
        print("签退：", response.json())
scanOut()
# if __name__ == "__main__":
#     if len(sys.argv) != 2:
#         print("请提供参数：1（签到）或 0（签退）")
#         sys.exit(1)

#     try:
#         action = int(sys.argv[1])
#         if action == 1:
#             scanIn()
#         elif action == 0:
#             scanOut()
#         else:
#             print("无效的参数，请输入 1 或 0")
#     except ValueError:
#         print("参数必须是数字 1 或 0")