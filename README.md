# Stock Monitor App

课程小组项目记录：一个 Android 客户端 + Spring Boot 后端的股票监测应用，包含用户注册登录、JWT 会话、行情查询、实时/日线数据处理和提醒配置。

## 目录

- `android-client/`：Java/Kotlin Android 客户端，基于 Retrofit/OkHttp 与后端通信。
- `src/`：Spring Boot 后端，包含认证、股票数据模型、定时处理器和 REST 控制器。

## 本人工作

负责客户端与后端的功能联调，参与登录注册、JWT 刷新、股票数据模型和定时行情处理链路；前端页面与部分接口由小组成员共同完成。

## 本地配置

复制项目后通过环境变量配置数据库和 JWT 密钥。仓库不包含 JKS、证书、数据库密码或真实服务地址：

```powershell
$env:DB_PASSWORD = 'your-local-password'
$env:JWT_ACCESS_SECRET = 'generate-a-long-random-value'
$env:JWT_REFRESH_SECRET = 'generate-another-long-random-value'
```

Android 客户端默认使用模拟器地址 `10.0.2.2`，真机调试时请在本地配置中替换为开发机地址。该仓库是课程项目归档，未承诺生产环境安全性。
