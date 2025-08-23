#!/bin/bash

rm -rf certificates
mkdir certificates
cd certificates

#Keystore Server
echo "SERVER KEYSTORE"
keytool -genkey -alias mediacollection_certificate_server -keyalg RSA -keystore KeyStore.p12 -keysize 4096 -storepass 123456 -dname "CN=MediaCollection Server Example, OU=com.example.mediacollection, O=forestj, L=AnyCity, S=AnyState, C=DE" -deststoretype pkcs12 -noprompt
openssl req -new -x509 -keyout ca-key-srv.pem -out ca-cert-srv.pem -days 365 -subj "/CN=MediaCollection Server Example/OU=com.example.mediacollection/O=forestj/L=AnyCity/ST=AnyState/C=DE" -passout pass:12345678
keytool -keystore KeyStore.p12 -alias mediacollection_certificate_server -certreq -file cert-file-srv.csr -storepass 123456
openssl x509 -req -CA ca-cert-srv.pem -CAkey ca-key-srv.pem -in cert-file-srv.csr -out cert-signed-srv.cer -days 365 -CAcreateserial -passin pass:12345678
keytool -keystore KeyStore.p12 -alias CARoot -import -file ca-cert-srv.pem -storepass 123456 -noprompt
keytool -keystore KeyStore.p12 -alias mediacollection_certificate_server -import -file cert-signed-srv.cer -storepass 123456 -noprompt

#Truststore Client
echo "CLIENT TRUSTSTORE"
keytool -keystore TrustStore.p12 -alias mediacollection_certificate_server -import -file cert-signed-srv.cer -storepass 123456 -deststoretype pkcs12 -noprompt

#Truststore Client as BKS file
echo "CLIENT TRUSTSTORE AS BKS"
keytool -exportcert -alias mediacollection_certificate_server -keystore KeyStore.p12 -storepass 123456 -file ca-cert-srv.der -storetype PKCS12
openssl x509 -inform der -in ca-cert-srv.der -out ca-cert-srv-android.pem
keytool -importcert -v -file ca-cert-srv-android.pem -alias mediacollection_certificate_server -keystore TrustStore.bks -storepass 123456 -storetype BKS -providerpath ../bks/bcprov-jdk15on-1.70.jar -providerclass org.bouncycastle.jce.provider.BouncyCastleProvider -noprompt

cd ..
