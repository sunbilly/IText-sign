@echo off
set OPENSSL_HOME=C:\OpenSSL-Win64
set PATH=%OPENSSL_HOME%\bin;%PATH%
mkdir %OPENSSL_HOME%\CA
cd /d %OPENSSL_HOME%\CA
echo "create subdir certs"
mkdir certs
mkdir newcerts
mkdir private
mkdir crl
echo "create file: index and serial"
echo 0>index.txt
echo 01>serial
echo "create rand file"
openssl rand -out private/.rand 1000
@rem echo %random% >> private/.rand
@echo off
set PATH=C:\OpenSSL-Win64\bin;%PATH%
echo create private key for rootca
@rem the password, set it as 123456
openssl genrsa -aes256 -out private/ca.key.pem 2048
echo generate root ca request
openssl req -new -key private/ca.key.pem -out private/ca.csr -subj "/C=CN/ST=GD/L=SZ/O=sunline/OU=914400007408467653/CN=www.sunline.cn"
echo create root ca cert
openssl x509 -req -days 10000 -sha1 -extensions v3_ca -signkey private/ca.key.pem -in private/ca.csr -out certs/ca.cer
echo convert the cert from cer into PKCS12
openssl pkcs12 -export -clcerts -in certs/ca.cer -inkey private/ca.key.pem -out certs/ca.p12
echo use keytool can query the pkcs12 cert status
keytool -list -keystore certs/ca.p12 -storetype pkcs12 -v -storepass 123456
echo create server ca
openssl genrsa -aes256 -out private/server.key.pem 2048
openssl req -new -key private/server.key.pem -out private/server.csr -subj "/C=CN/ST=GD/L=SZ/O=sunline/OU=914400007408467653/CN=www.sunline.cn"
openssl x509 -req -days 3650 -sha1 -extensions v3_req -CA certs/ca.cer -CAkey private/ca.key.pem -CAserial ca.srl -CAcreateserial -in private/server.csr -out certs/server.cer
openssl pkcs12 -export -clcerts -inkey private/server.key.pem -in certs/server.cer -out certs/server.p12
echo create client ca
openssl genrsa -aes256 -out private/client.key.pem 2048
openssl req -new -key private/client.key.pem -out private/client.csr -subj "/C=CN/ST=GD/L=SZ/O=sunline/OU=914400007408467653/CN=www.sunline.cn"
openssl ca -days 3650 -in private/client.csr -out certs/client.cer -cert certs/ca.cer -keyfile private/ca.key.pem
openssl pkcs12 -export -clcerts -inkey private/client.key.pem -in certs/client.cer -out certs/client.p12
cp -r certs certs.new
