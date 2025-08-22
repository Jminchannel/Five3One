# 531训练法助手 - 部署指南

## 📦 项目结构

```
531训练计划/
├── index.html              # 主应用页面
├── app.js                  # 核心JavaScript逻辑
├── styles.css              # 补充CSS样式
├── app_prototype.html      # 设计原型展示
├── README.md              # 项目说明文档
└── DEPLOYMENT.md          # 部署指南
```

## 🚀 部署方式

### 1. 本地直接运行
最简单的方式，适合个人使用：

```bash
# 直接打开index.html文件
open index.html

# 或双击index.html文件
```

### 2. 本地HTTP服务器
推荐用于开发和测试：

```bash
# 使用Python (推荐)
python -m http.server 8000
# 访问 http://localhost:8000

# 使用Node.js
npx serve .
# 或 npx http-server

# 使用PHP
php -S localhost:8000

# 使用Ruby
ruby -run -e httpd . -p 8000
```

### 3. 静态网站托管

#### GitHub Pages
1. 创建GitHub仓库
2. 上传所有文件到仓库
3. 在仓库设置中启用GitHub Pages
4. 选择主分支作为源
5. 访问 `https://yourusername.github.io/your-repo-name`

#### Netlify
1. 访问 [netlify.com](https://netlify.com)
2. 拖拽整个项目文件夹到Netlify
3. 自动部署并获得访问链接
4. 支持自定义域名

#### Vercel
1. 访问 [vercel.com](https://vercel.com)
2. 连接GitHub仓库或直接上传
3. 自动部署并获得访问链接

#### Firebase Hosting
```bash
npm install -g firebase-tools
firebase login
firebase init hosting
firebase deploy
```

### 4. 云服务器部署

#### Nginx配置示例
```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/531训练计划;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # 启用gzip压缩
    gzip on;
    gzip_types text/css application/javascript text/html;

    # 设置缓存头
    location ~* \.(css|js|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

#### Apache配置示例
```apache
<VirtualHost *:80>
    ServerName your-domain.com
    DocumentRoot /path/to/531训练计划
    
    <Directory /path/to/531训练计划>
        Options Indexes FollowSymLinks
        AllowOverride All
        Require all granted
    </Directory>
    
    # 启用压缩
    LoadModule deflate_module modules/mod_deflate.so
    <Location />
        SetOutputFilter DEFLATE
        SetEnvIfNoCase Request_URI \
            \.(?:gif|jpe?g|png)$ no-gzip dont-vary
    </Location>
</VirtualHost>
```

## 📱 PWA配置

### 添加PWA支持
创建 `manifest.json`：

```json
{
  "name": "531训练法助手",
  "short_name": "531助手",
  "description": "专业的力量训练计划助手",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#3b82f6",
  "icons": [
    {
      "src": "icon-192.png",
      "sizes": "192x192",
      "type": "image/png"
    },
    {
      "src": "icon-512.png",
      "sizes": "512x512",
      "type": "image/png"
    }
  ]
}
```

在 `index.html` 中添加：

```html
<link rel="manifest" href="manifest.json">
<meta name="theme-color" content="#3b82f6">
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="default">
<meta name="apple-mobile-web-app-title" content="531助手">
```

### Service Worker (可选)
创建 `sw.js` 实现离线功能：

```javascript
const CACHE_NAME = '531-app-v1';
const urlsToCache = [
  '/',
  '/index.html',
  '/app.js',
  '/styles.css',
  // 添加其他资源
];

self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(urlsToCache))
  );
});

self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request)
      .then(response => response || fetch(event.request))
  );
});
```

## 🔧 配置优化

### 性能优化
1. **启用gzip压缩**：减少传输文件大小
2. **设置缓存头**：提高重复访问速度
3. **图片优化**：使用WebP格式和适当尺寸
4. **CSS/JS压缩**：生产环境压缩代码

### 安全设置
添加安全头部：

```
Content-Security-Policy: default-src 'self' https://cdn.tailwindcss.com https://cdnjs.cloudflare.com; script-src 'self' 'unsafe-inline' https://cdn.tailwindcss.com; style-src 'self' 'unsafe-inline' https://cdnjs.cloudflare.com;
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Referrer-Policy: strict-origin-when-cross-origin
```

### 监控和分析
可以添加的监控工具：

1. **Google Analytics**：用户行为分析
2. **Sentry**：错误监控
3. **PageSpeed Insights**：性能监控

## 🌐 域名和SSL

### 域名配置
1. 购买域名（推荐支持中文的域名商）
2. 配置DNS指向你的服务器IP
3. 等待DNS传播（通常24-48小时）

### SSL证书
免费SSL证书获取：

```bash
# 使用Let's Encrypt
sudo certbot --nginx -d your-domain.com

# 或使用acme.sh
curl https://get.acme.sh | sh
acme.sh --issue -d your-domain.com --nginx
```

## 📊 部署检查清单

### 部署前检查
- [ ] 所有文件路径正确
- [ ] 外部资源CDN可访问
- [ ] 本地测试功能正常
- [ ] 浏览器兼容性测试
- [ ] 移动端适配测试

### 部署后验证
- [ ] 网站可正常访问
- [ ] 所有功能正常工作
- [ ] 数据可正常保存
- [ ] 性能表现良好
- [ ] SSL证书有效（如适用）

### 维护检查
- [ ] 定期备份用户数据
- [ ] 监控错误日志
- [ ] 更新依赖版本
- [ ] 性能优化调整

## 🔄 更新部署

### 版本更新流程
1. 在开发环境测试新功能
2. 更新版本号
3. 打包部署文件
4. 备份当前版本
5. 部署新版本
6. 验证功能正常
7. 通知用户更新

### 数据迁移
如需更新数据结构：

```javascript
// 在app.js中添加迁移逻辑
const migrateData = (oldData, oldVersion, newVersion) => {
  if (oldVersion < '1.1.0') {
    // 执行1.1.0的数据迁移
    oldData.newField = defaultValue;
  }
  return oldData;
};
```

## 🎯 生产环境优化

### 代码优化
```bash
# 压缩JavaScript
npx terser app.js -o app.min.js

# 压缩CSS
npx clean-css-cli styles.css -o styles.min.css

# 优化HTML
npx html-minifier --collapse-whitespace --remove-comments index.html -o index.min.html
```

### 图片优化
```bash
# 安装imagemin
npm install -g imagemin-cli imagemin-webp

# 转换为WebP
imagemin images/*.png --plugin=webp > images/
```

## 📞 技术支持

如在部署过程中遇到问题：

1. 检查浏览器控制台错误信息
2. 验证文件路径和权限
3. 查看服务器错误日志
4. 参考常见问题解决方案

**记住：531训练法助手是一个纯前端应用，无需数据库和后端服务器，部署相对简单！**

---

*部署完成后，你就可以随时随地进行科学的力量训练了！💪*
