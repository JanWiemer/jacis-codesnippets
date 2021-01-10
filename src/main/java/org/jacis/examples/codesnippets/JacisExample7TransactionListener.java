/*
 * Copyright (c) 2016. Jan Wiemer
 */

package org.jacis.examples.codesnippets;

import org.jacis.container.JacisContainer;
import org.jacis.container.JacisObjectTypeSpec;
import org.jacis.container.JacisTransactionHandle;
import org.jacis.examples.codesnippets.JacisExample1GettingStarted.Account;
import org.jacis.plugin.JacisTransactionListener;
import org.jacis.plugin.objectadapter.cloning.JacisCloningObjectAdapter;
import org.jacis.store.JacisStore;

/**
 * Example 7: Transaction Listener.
 *
 * @author Jan Wiemer
 */
public class JacisExample7TransactionListener {

  static class ExampleJacisTransactionListener implements JacisTransactionListener {

    @Override
    public void afterPrepare(JacisContainer container, JacisTransactionHandle tx) {
      System.out.println("prepare finished");
    }

    @Override
    public void afterCommit(JacisContainer container, JacisTransactionHandle tx) {
      System.out.println("commit finished");
    }

    @Override
    public void afterRollback(JacisContainer container, JacisTransactionHandle tx) {
      System.out.println("rollback finished");
    }

  }

  public static void main(String[] args) {

    JacisContainer container = new JacisContainer();
    // register an example transaction listener
    JacisTransactionListener listener = new ExampleJacisTransactionListener();
    container.registerTransactionListener(listener);

    JacisObjectTypeSpec<String, Account, Account> objectTypeSpec = //
        new JacisObjectTypeSpec<>(String.class, Account.class, new JacisCloningObjectAdapter<>());
    JacisStore<String, Account> store = container.createStore(objectTypeSpec).getStore();

    container.withLocalTx(() -> {
      store.update("account1", new Account("account1").withdraw(100));
    });
    container.withLocalTx(() -> {
      store.update("account1", store.get("account1").deposit(1000));
    });
    try {
      container.withLocalTx(() -> {
        store.update("account1", store.get("account1").deposit(1000));
        throw new RuntimeException("force rollback");
      });
    } catch (RuntimeException e) {
      // expected
    }
  }
}
