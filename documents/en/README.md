# Active Auth

An open-sourced cloud platform PBAC Authentication & Authorization Center.

[English](README.md) | [简体中文](../zh-Hans/README.md)

## Features

(README.md document and wikipages will publish on release of first version, please wait.)

### Locating Resources

The resource locator used in this project is a string with rich field content, separated by a colon `:`, which can
clearly describe the system, subsystem, microservice, data center, and user to which the resource belongs. It can also
express some simple resource hierarchical relationships in catalog style.

Here is an example:

> arn:activecloud-cn:ecs:cn-north-3:7611:volume/vol-8678eY3109N946oVsq

More Details Ref: [01 Locating Resources](01-Locating-Resources.md)

### Describing Authority Policies

Authorization strategy is the core concept of PBAC model certification framework or certification center. Permission
policy is usually similar to the predicate-object phrase in natural language, which describes: "ALLOW or DENY **certain
OPERATIONS** on **certain RESOURCES**."

The creator of the permission policy grants it to others, and the entire PBAC becomes complete, which is described
as: "**SOMEONE** allows or denies **SOMEONE** to perform **OPERATIONS** on **certain RESOURCES**".

The following policy description is an example:

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

More Details Ref: [02 Describing Authority Policies](02-Describing-Authority-Policies.md)

## Maintainers

[Okeyja Teung](https://github.com/Okeyja)

## Sponsors

Not any sponsors.