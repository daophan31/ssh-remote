## Hướng dẫn sử dụng
### Yêu cầu
- docker
- java

## Build images ubuntu-ssh
- trong folder của project run:

```bash
docker build -t ubuntu-image:0.1 .
```

- kiểm tra image sau khi build
```bash
docker images
```

- run container
```bash
docker run -td --name ubt1 -p 8222:22 ubuntu-ssh:0.1
```

- tạo 1 user mới cho việc test ssh
  - trước tiên attach terminal của container
  - thêm user

- attach terminal của container
```bash
docker exec -it ubt1 /bin/bash
```

- thêm user
```bash
adduser [nameuser]
.......
```

## Start ứng dụng
```bash
java -jar ./target/terminal-remote-0.0.1-SNAPSHOT.jar
```

## sử dụng
- truy cập localhost:8080 trên trình duyệt
- trong host input nhập: 127.0.0.1 , port: 8080
- username và password thì nhập thông tin vừa add