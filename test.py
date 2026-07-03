import urllib.request
try:
    req = urllib.request.urlopen("http://127.0.0.1:8080/perm-menu-manager/api/fun-permission/page")
    print(req.read().decode())
except urllib.error.HTTPError as e:
    print("HTTP ERROR:", e.code)
    print(e.read().decode())
