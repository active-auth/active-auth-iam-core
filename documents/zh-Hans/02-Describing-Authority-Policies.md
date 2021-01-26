# 描述权限策略

权限策略是 PBAC 模式的认证框架或认证中心的核心概念。权限策略通常类似于自然语言中的谓宾短语，描述的是：“允许或拒绝对**某资源**进行**某操作**”。

权限策略的创建者将它授予其他人，则整个 PBAC 趋于完整，即描述成：“**谁**允许或拒绝**谁**对**某资源**进行**某操作**”。

## 例子

例如下面的策略描述：

```json
{
  "name": "Allowing43ToListAndDeleteMyBooks",
  "effect": "ALLOW",
  "actions": [
    "bookshelf:ListBooks",
    "bookshelf:DeleteBooks"
  ],
  "resources": [
    "arn:cloudapp:bookshelf::31:bought-book/*",
    "arn:cloudapp:bookshelf::31:shopping-cart/*"
  ]
}
```

上述例子中，获得这条策略授权的用户（Principal），将有权限列举已购书籍、购物车书籍，或删除已购书籍、购物车书籍。

## 授权策略

策略所有者将有权限授权任何自己的资源给其他人。

## 挑战策略

任何一个用户检查是否拥有特定用户的资源，需要由 Agent 服务携带谓语和宾语（Agent 可选自带 Principal 主语或者直接用 Principal 主语），
根据被授权允许、禁止或未被授权的情况，返回相关的结果。

## 策略亦是资源

一个登录的用户（Principal）创建的一个策略，也是一个资源，遵守资源命名规则。例如上面创建的权限策略创建成功后，得到的资源定位符为：

> arn:cloudapp:iam::77:policy/62701

如果有其他用户（Principal）要操作这个策略（修改、删除、链式授权），则都需要原拥有者的相关操作许可。
