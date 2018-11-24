# Distributed unique ID generators

## Usage

Maven pom.xml:

```xml
<dependency>
  <groupId>com.github.pingaz</groupId>
  <artifactId>idgen</artifactId>
  <version>0.1.1-ALPHA</version>
</dependency>
```

New a generator and create a id:

```java
IdGenerator idGenerator = IdGenerator.createLongTimeBased();
idGenerator.nextLong();
```

## Long UID

It is a distributed unique ID generator inspired by Twitter's Snowflake.
<p>
A Long UID is composed of

```
 1 bit for sign
35 bit for time in units of 128 msec
28 bit for a generator id and a sequence number
   - 0 ~ 24 bit for a generator id
   - 4 ~ 28 bit for a sequence number ( 28 minus generator id length)
   - the total is 28 bit.
```

In order to get a new unique ID, you just have to new a generator and call the method nextId.

- Default generator - with network IP V4 address of the 12 bits

  up  to 512,000 per second until October 2154

```java
IdGenerator idgen = IdGenerator.createLongTimeBased();
```

- IP v4 Class B generator - with network IP V4 address of the 3th and 4th byte.

  up  to 32,000 per second until October 2154

```java
IdGenerator idgen = IdGenerator.createLongTimeBasedWithIpClassB();
```

- IP v4 Class C generator - with network IP V4 address of the 4th byte.

  up  to 8192,000 per second until October 2154

```java
IdGenerator idgen = IdGenerator.createLongTimeBasedWithIpClassA();
```

- ...

## IUID

It is a distributed unique ID generator inspired by Mongo's ObjectId.
<p>
A IUID is composed of

```
 1 bit for sign
47 bit for time in units of msec
32 bit for machine id
16 bit for process id
32 bit for a sequence number
```

In order to get a new unique ID, you just have to new a generator and call the method nextId.

```java
IdGenerator idgen = IdGenerator.createIUID();
```

