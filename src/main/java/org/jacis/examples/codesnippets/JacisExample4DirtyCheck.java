/*
 * Copyright (c) 2016. Jan Wiemer
 */

package org.jacis.examples.codesnippets;

import org.jacis.container.JacisContainer;
import org.jacis.container.JacisObjectTypeSpec;
import org.jacis.plugin.dirtycheck.object.AbstractReadOnlyModeAndDirtyCheckSupportingObject;
import org.jacis.plugin.objectadapter.cloning.JacisCloneable;
import org.jacis.plugin.objectadapter.cloning.JacisCloningObjectAdapter;
import org.jacis.store.JacisStore;

/**
 * Example 4: showing the default dirty check mechanism.
 *
 * @author Jan Wiemer
 */
public class JacisExample4DirtyCheck {

  // Account object with dirty check support
  static class Account extends AbstractReadOnlyModeAndDirtyCheckSupportingObject implements JacisCloneable<Account> {

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
    JacisObjectTypeSpec<String, Account, Account> objectTypeSpec = //
        new JacisObjectTypeSpec<>(String.class, Account.class, new JacisCloningObjectAdapter<>());
    objectTypeSpec.setObjectBasedDirtyCheck(); // *** switch on automatic dirty check ***
    JacisStore<String, Account> store = container.createStore(objectTypeSpec).getStore();

    container.withLocalTx(() -> { // First we create a test account...
      store.update("account1", new Account("account1").deposit(100));
    });
    container.withLocalTx(() -> { // Output balance before modification...
      System.out.println("balance: " + store.get("account1").getBalance());
    });
    container.withLocalTx(() -> { // Modify the balance relying on dirty check
      store.get("account1").deposit(500); // *** no update call! ***
    });
    container.withLocalTx(() -> { // Output balance after modification...
      System.out.println("balance: " + store.get("account1").getBalance());
    });

  }
}
