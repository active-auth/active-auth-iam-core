# Active Auth

An open-sourced cloud platform PBAC Authentication & Authorization Center.

## Features

(README.md document and wikipages will publish on release of first version, please wait.)

### Locating Resources

A resource locator looks like

> prefix:partition:service:region:account-id:resource/paths/and/id

Here is an example:

> wecloud:wecloud-cn:vhs:cn-north-3:7611:volume/vol-8678eY3109N946oVsq

| field name | example | explaining |
| ---- | ---- | ---- |
| prefix | wecloud | Your cloud platform name. <br> Example shows your cloud platform named `wecloud`. |
| partition | wecloud-cn | Your cloud platform partition, used to distinguish different commercial operating area. <br> Example shows an area operating in Mainland China to adapt to local legal compliance requirements, named `wecloud-cn`. |
| service | vhs | Your subsystem/cloud-service name, usually using when you have different service such as Virtual Hosting, Object Storage, Virtual Network, AI, Big Data services etc. <br> Example shows its a Virtual Hosting Service(`VHS`). |
| region | cn-north-3 | Your subsystem region/datacenter code. <br> Example shows that resource is located in QingDao, Shandong, China(`cn-north-3`) |
| account-id | 7611 | Principal ID. Example shows that resource belongs to user `7611`. |
| resource/paths/and/id | volume/vol-8678eY3109N946oVsq | Resource paths and resource id. <br> Example shows that resource is a `volume`, id: `vol-8678eY3109N946oVsq`. |

## Maintainers

[Okeyja Teung](https://github.com/Okeyja)

## Sponsors

Not any sponsors.