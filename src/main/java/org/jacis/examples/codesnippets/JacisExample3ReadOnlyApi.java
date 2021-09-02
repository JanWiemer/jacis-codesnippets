/*
 * Copyright (c) 2016. Jan Wiemer
 */

package org.jacis.examples.codesnippets;

import org.jacis.container.JacisContainer;
import org.jacis.container.JacisObjectTypeSpec;
import org.jacis.exception.ReadOnlyException;
import org.jacis.plugin.objectadapter.cloning.JacisCloneable;
import org.jacis.plugin.objectadapter.cloning.JacisCloningObjectAdapter;
import org.jacis.plugin.readonly.object.AbstractReadOnlyModeSupportingObject;
import org.jacis.store.JacisStore;

/**
 * Example 3: showing usage of read only objects.
 *
 * @author Jan Wiemer
 */
public class JacisExample3ReadOnlyApi {

  // Account object with support to switch between read-only-mode and read-write-mode
  static class Account extends AbstractReadOnlyModeSupportingObject implements JacisCloneable<Account> {

    private final String name;
    private long balance;

    public Account(String name) {
      this.name = name;
    }

    @Override
    public Account clone() {
      return (Account) super.clone();
    }

    public Account deposit(long amount) {
      checkWritable();
      balance += amount;
      return this;
    }

    public Account withdraw(long amount) {
      checkWritable();
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

  public static void main(String[] args) {
    JacisContainer container = new JacisContainer();
    JacisObjectTypeSpec<String, Account, Account> objectTypeSpec = new JacisObjectTypeSpec<>(String.class, Account.class, new JacisCloningObjectAdapter<>());
    JacisStore<String, Account> store = container.createStore(objectTypeSpec).getStore();

    // First we create some accounts to have some test data...

    container.withLocalTx(() -> {
      store.update("account0", new Account("account0").withdraw(100));
      store.update("account1", new Account("account1").deposit(100));
      store.update("account2", new Account("account2").deposit(200));
      store.update("account3", new Account("account3").deposit(300));
      store.update("account4", new Account("account4").deposit(400));
      store.update("account5", new Account("account5").deposit(500));
      store.update("account6", new Account("account6").deposit(600));
      store.update("account7", new Account("account7").deposit(700));
      store.update("account8", new Account("account8").deposit(800));
      store.update("account9", new Account("account9").deposit(900));
    });

    // Now we show some examples how to use read only objects:
    container.withLocalTx(() -> {

      // using stream methods (compute the total balance of all stored accounts):
      System.out.println("total balance=" + store.streamReadOnly().mapToLong(Account::getBalance).sum());

      // using a filter (count accounts with a balance > 500):
      System.out.println("#>500=" + store.streamReadOnly(acc -> acc.getBalance() > 500).count());

      // updating a read only object fails:
      try {
        store.update("account1", store.getReadOnly("account1").deposit(10));
      } catch (ReadOnlyException e) {
        System.out.println("caught expected Exception: " + e);
      }

      // looping through the elements (adding writing an output)
      store.streamReadOnly().forEach(acc -> {
        if (acc.getBalance() < 0) {
          acc = store.get(acc.getName());
          acc.deposit(42);
        }
      });
    });
    System.out.println("get readonly without tx: " + store.getReadOnly("account1"));
  }
}
