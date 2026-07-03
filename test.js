const http = require('http');
http.get('http://127.0.0.1:8080/perm-menu-manager/api/fun-permission/page', (resp) => {
  let data = '';
  resp.on('data', (chunk) => { data += chunk; });
  resp.on('end', () => { console.log(data); });
}).on("error", (err) => {
  console.log("Error: " + err.message);
});
