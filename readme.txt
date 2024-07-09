## 打包im,多域名多线路

AppConfig.java
图片配置
```
    public static final String APP_URL = "https://d2pgq5kklrcat6.cloudfront.net";
    public static final String IMG_URL = "https://d2pgq5kklrcat6.cloudfront.net/imapp/ymtest/1.jpg";
```

如果不用图片
```
    public static final String IMG_URL = "";
    public static String[] HOST_LIST = {
            "http://75.2.9.72:8881/config",
            "http://75.2.9.72:8881/config"
    };
```
