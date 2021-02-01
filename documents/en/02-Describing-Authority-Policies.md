# Describing Authority Policies

Authorization Policy is the core concept of PBAC model certification framework or certification center. Permission
policy is usually similar to the predicate-object phrase in natural language, which describes: "ALLOW or DENY **certain
OPERATIONS** on **certain RESOURCES**."

The creator of the permission policy grants it to others, and the entire PBAC becomes complete, which is described
as: "**SOMEONE** allows or denies **SOMEONE** to perform **OPERATIONS** on **certain RESOURCES**".

## Granting Policies

The policy owner will have the authority to authorize any own resources to others.
The authorized person will be qualified to operate the corresponding resource.

## Challenging Policies

To check whether a user has the resources of a specific user, the Agent service 
needs to carry the predicate and object (Agent can bring a custom Principal subject
or directly use the currently requested Principal).

Generally, the resource owner's challenge to perform any operation on his own resources
is **unconditionally allowed**, unless prohibited by the superior user or prohibited by
the globally added policy.

## Policies Are Also Resources

A policy created by a logged-in user (Principal) is also a resource, and it follows the
resource naming rules. For example, after the permission policy created above is successfully
created, the resource locator obtained is:

> arn:cloudapp:iam::77:policy/62701

If there are other users (Principal) who want to operate this policy (modify, delete, chain authorization),
they all need the relevant operation permission of the original owner.


## Example

### Common Access Granting

Suppose that User-A (UID:31) creates a strategy like this:

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

Locator:

> arn:cloudapp:iam::31:policy/62099

In the above example, User-B (UID:98) authorized by this policy will have the authority to list bought books,
shopping cart books, or delete bought books, shopping cart books.

### Chain Granting

If user B (UID: 98) goes to "delete user A (UID: 31) shopping cart books" action is authorized to user C (UID: 102)

User B (UID: 98) needs to be granted the following policy permissions by user A (UID: 31):

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

At this time, according to the rules of resource wildcards, user B (UID:98) can allocate the following resources infinitely.

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

### Chain Challenging

Each policy will be traced back to its resource owner, who has experienced the chain authorization of users at the A-F level.
When user F (UID: 271) challenges the following:

Action:
> bookshelf:DeleteBooks

Resource:
> arn:cloudapp:bookshelf::31:shopping-cart/sci-fi/liucixin/three-body-3-v2020k2

The stack paging process of chain challenge check is as follows:

> F Challenging, Asking if F has authority
>
> F is granted by E, Asking if E has authority
>
> E is granted by D, Asking if D has authority
>
> D is granted by C, Asking if C has authority
>
> C is granted by B, Asking if B has authority
>
> B is granted by A, Asking if A has authority
>
> Resource is owned by A, A has authority, allowing to each level, pop the paging stack

Once an intermediate link is not authorized, the stack is interrupted, and the entire authorization will be denied:

> F Challenging, Asking if F has authority
>
> F is granted by E, Asking if E has authority
>
> E is granted by D, Asking if D has authority
>
> D is not allowed, and resource is not owned by D, denying to each level, pop the paging stack

### Revoking

In the above example, user A (UID: 31), the ultimate owner of the resource, has revoked all permissions for user B (UID: 98),
and user C (UID: 102) and subsequent chain authorization will also be invalid.