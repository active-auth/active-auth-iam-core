# Describing Authority Policies

Authorization strategy is the core concept of PBAC model certification framework or certification center. Permission
policy is usually similar to the predicate-object phrase in natural language, which describes: "ALLOW or DENY **certain
OPERATIONS** on **certain RESOURCES**."

The creator of the permission policy grants it to others, and the entire PBAC becomes complete, which is described
as: "**SOMEONE** allows or denies **SOMEONE** to perform **OPERATIONS** on **certain RESOURCES**".

## Example

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

## Strategies Are Also Resources

A policy created by a logged-in user (Principal) is also a resource, and it follows the
resource naming rules. For example, after the permission policy created above is successfully
created, the resource locator obtained is:

> arn:cloudapp:iam::77:policy/62701

If there are other users (Principal) who want to operate this policy (modify, delete, chain authorization),
they all need the relevant operation permission of the original owner.
