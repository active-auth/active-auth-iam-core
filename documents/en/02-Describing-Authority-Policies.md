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