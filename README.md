# Active Auth

An open-sourced cloud platform PBAC Authentication & Authorization Center.

[English](README.md) | [简体中文](README-zh_Hans.md)

## Features

(README.md document and wikipages will publish on release of first version, please wait.)

### Locating Resources

#### Locator Style

A resource locator looks like

> prefix:partition:service:region:account-id:resource/paths/and/id

Here is an example:

> arn:activecloud-cn:ecs:cn-north-3:7611:volume/vol-8678eY3109N946oVsq

| field name | example | explaining |
| ---- | ---- | ---- |
| prefix | arn | Your cloud platform name. <br> Example shows that locator named Activecloud Resource Name `ARN`. |
| partition | activecloud-cn | Your cloud platform partition, used to distinguish different commercial operating area. <br> Example shows an area operating in Mainland China to adapt to local legal compliance requirements, named `activecloud-cn`. |
| service | ecs | Your subsystem/cloud-service name, usually using when you have different service such as Virtual Hosting, Object Storage, Virtual Network, AI, Big Data services etc. <br> Example shows its a Elastic Compute Service(`ECS`). |
| region | cn-north-3 | Your subsystem region/datacenter code. <br> Example shows that resource is located in QingDao, Shandong, China(`cn-north-3`) |
| account-id | 7611 | Principal ID. Example shows that resource belongs to user `7611`. |
| resource/paths/and/id | volume/vol-8678eY3109N946oVsq | Resource paths and resource id. <br> Example shows that resource is a `volume`, id: `vol-8678eY3109N946oVsq`. |

#### Legal Empty Field in Locator

Field `region` and `account-id` is allowed to be empty. Here is a legal example:

> arn:activecloud-cn:oss:::my-website-static-media

A storage bucket is named uniquely in a global namespace, or any other system resource owner of whom can't be included in
the locator, in that case you can keep field `region` and `account-id` empty to describe a resource.

## Maintainers

[Okeyja Teung](https://github.com/Okeyja)

## Sponsors

Not any sponsors.