/*
 * Copyright (c) 2016. Jan Wiemer
 */

package org.jacis.examples.codesnippets;

import org.jacis.container.JacisContainer;
import org.jacis.container.JacisObjectTypeSpec;
import org.jacis.plugin.objectadapter.cloning.JacisCloneable;
import org.jacis.plugin.objectadapter.cloning.JacisCloningObjectAdapter;
import org.jacis.plugin.txadapter.local.JacisLocalTransaction;
import org.jacis.store.JacisStore;

/**
 * Example 1: showing basic usage of a JACIS store.
 *
 * @author Jan Wiemer
 */
public class JacisExample1GettingStarted {

  // First we create a simple example class of objects that shall be stored in a
  // transactional store.
  // The class implements the JacisCloneable interface to enable the store to
  // clone the object without using reflection.
  static class Account implements JacisCloneable<Account> {

    private final String name;
    private long balance;

    public Account(String name) {
      this.name = name;
    }

    @Override
    public Account clone() {
      try {
        return (Account) super.clone();
      } catch (CloneNotSupportedException e) {
        throw new InternalError("Could not clone " + this.getClass().getName());
      }
    }

    public Account deposit(long amount) {
      balance += amount;
      return this;
    }

    public Account withdraw(long amount) {
      balance -= amount;
      return this;
    }

    public String getName() {
      return name;
    }

    public long getBalance() {
      return balance;
    }

    // ...

  }

  // In the main method of the Example we show how to create a store for the example object
  // and how to do some simple operations on the store:
  public static void main(String[] args) throws Throwable {

    // first we initialize a JACIS container:
    JacisContainer container = new JacisContainer();

    // now we create a store for our example object:
    JacisObjectTypeSpec<String, Account, Account> objectTypeSpec = //
        new JacisObjectTypeSpec<>(String.class, Account.class, new JacisCloningObjectAdapter<>());
    JacisStore<String, Account> store = container.createStore(objectTypeSpec).getStore();

    // now we start a transaction
    JacisLocalTransaction tx = container.beginLocalTransaction();
    // and create a new Account
    Account account1 = new Account("account1");
    // modifications (and creation of new objects) have to be notified to the store by calling:
    store.update(account1.getName(), account1);
    // now we internalCommit the transaction. Afterwards all other transactions can see our new Account
    tx.commit();

    // we use a helper method executing some code inside a transaction to deposit some money on the account.
    // Note that the update is necessary, otherwise the change will be lost after internalCommit (try it).
    container.withLocalTx(() -> {
      Account acc = store.get("account1");
      acc.deposit(100);
      store.update("account1", acc);
    });

    // now we use another transaction to check the balance of the Account
    container.withLocalTx(() -> {
      Account acc = store.get("account1");
      System.out.println("balance of " + acc.getName() + ": " + acc.getBalance());
    });

    // now we withdraw some money and simulate an exception causing the transaction to be rolled back
    try {
      container.withLocalTx(() -> {
        Account acc = store.get("account1");
        acc.withdraw(10);
        store.update("account1", acc);
        throw new RuntimeException("Error in transaction!");
      });
    } catch (RuntimeException e) {
      System.out.println("Expected exception " + e);
      // expected
    }

    // now we again check the balance of the Account to see nothing is withdrawn
    container.withLocalTx(() -> {
      Account acc = store.get("account1");
      System.out.println("balance of " + acc.getName() + ": " + acc.getBalance());
    });

    // to demonstrate the isolation we start one transaction, do a modification and check it in another transaction:
    JacisLocalTransaction tx1 = container.beginLocalTransaction();
    Account acc = store.get("account1");
    System.out.println("balance of " + acc.getName() + " before TX: " + acc.getBalance());
    acc.deposit(1000);
    store.update(acc.getName(), acc);
    System.out.println("balance of " + acc.getName() + " in TX-1 " + acc.getBalance());
    Thread thread = new Thread() {
      @Override
      public void run() {
        JacisLocalTransaction tx2 = container.beginLocalTransaction();
        System.out.println("balance of " + acc.getName() + " in TX-2: " + store.get("account1").getBalance());
        tx2.commit();
      }
    };
    thread.start();
    thread.join();
    tx1.commit();
  }
}
