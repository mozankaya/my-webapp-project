import requests
resp = requests.post('http://localhost:8080/my-webapp-project/api/tasks', json={'title':'python-body-test','completed':False})
print('status', resp.status_code)
print('body:', resp.text)
