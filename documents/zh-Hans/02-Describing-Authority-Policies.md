# 描述权限策略

权限策略是 PBAC 模式的认证框架或认证中心的核心概念。权限策略通常类似于自然语言中的谓宾短语，描述的是：“允许或拒绝对**某资源**进行**某操作**”。

权限策略的创建者将它授予其他人，则整个 PBAC 趋于完整，即描述成：“**谁**允许或拒绝**谁**对**某资源**进行**某操作**”。

## 授权策略

策略所有者将有权限授权任何自己的资源给其他人。被授权的人将有资格对相应的资源进行操作。

## 挑战策略

任何一个用户检查是否拥有特定用户的资源，需要由 Agent 服务携带谓语和宾语（Agent 可带自定义的 Principal 主语或者直接用当前请求的 Principal）。

通常，资源拥有者挑战对自己的资源进行任何操作是被**无条件允许**的，除非被上级用户禁止，或被全局添加的策略禁止。

## 策略亦是资源

一个登录的用户（Principal）创建的一个策略，也是一个资源，遵守资源命名规则。例如上面创建的权限策略创建成功后，得到的资源定位符为：

> arn:cloudapp:iam::77:policy/62701

如果有其他用户（Principal）要操作这个策略（修改、删除、链式授权），则都需要原拥有者的相关操作许可。

## 例子

### 常规访问授权

用户 A（UID:31）创建了这样的策略：

```json
{
  "name": "AllowingToListAndDeleteMyBooks",
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

Locator:

> arn:cloudapp:iam::31:policy/62099

上述例子中，获得这条策略授权的用户 B（UID:98），将有权限列举已购书籍、购物车书籍，或删除已购书籍、购物车书籍。

### 链式授权

如果用户 B（UID:98）要将“删除用户A（UID:31）的购物车书籍”的动作授权给用户C（UID:102）

用户 B（UID:98）需要被用户 A（UID:31）授予下面策略的权限：

```json
{
  "name": "AllowingToDeleteMyBooksInCart",
  "effect": "ALLOW_FOR_CHAIN",
  "actions": [
    "bookshelf:DeleteBooks"
  ],
  "resources": [
    "arn:cloudapp:bookshelf::31:shopping-cart/*"
  ]
}
```

Locator:

> arn:cloudapp:iam::31:policy/72170

这时候，按照资源通配符的规则，用户 B（UID:98）可以将下面这类的资源无限分配出去。

Action:
> bookshelf:DeleteBooks

Resources:
> arn:cloudapp:bookshelf::31:shopping-cart/*
> 
> arn:cloudapp:bookshelf::31:shopping-cart/sci-fi/*
> 
> arn:cloudapp:bookshelf::31:shopping-cart/old/12801
> 
> ......

### 链式挑战策略

每一个策略都会被追溯到其资源拥有者，经历过用户 A-F 层级的用户的链式授权，当用户 F（UID:271）进行如下挑战的时候：

Action:
> bookshelf:DeleteBooks

Resource:
> arn:cloudapp:bookshelf::31:shopping-cart/sci-fi/liucixin/three-body-3-v2020k2

链式挑战检查的栈式寻呼过程如下：

> F 正在挑战，询问 F 有无权限
> 
> F 被 E 授权允许，询问 E 有无权限
> 
> E 被 D 授权允许，询问 D 有无权限
> 
> D 被 C 授权允许，询问 C 有无权限
> 
> C 被 B 授权允许，询问 B 有无权限
> 
> B 被 A 授权允许，询问 A 有无权限
> 
> 资源被 A 所拥有，A 有权限，逐级向上允许，弹出寻呼栈

一旦中间某环节未被授权，栈被打断，整个授权将被禁止：

> F 正在挑战，询问 F 有无权限
>
> F 被 E 授权允许，询问 E 有无权限
>
> E 被 D 授权允许，询问 D 有无权限
>
> D 未被允许，且资源不属于 D，逐级向上禁止，弹出寻呼栈

### 回收

上述例子中，资源最终拥有者用户 A（UID:31）回收了所有对用户 B（UID:98）的权限，用户 C（UID:102）及后续的链式授权也将失效。
