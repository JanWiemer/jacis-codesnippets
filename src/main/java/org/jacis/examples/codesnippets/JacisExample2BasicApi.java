/*
 * Copyright (c) 2016. Jan Wiemer
 */

package org.jacis.examples.codesnippets;

import java.util.Comparator;
import java.util.stream.Collectors;

import org.jacis.container.JacisContainer;
import org.jacis.container.JacisObjectTypeSpec;
import org.jacis.examples.codesnippets.JacisExample1GettingStarted.Account;
import org.jacis.plugin.objectadapter.cloning.JacisCloningObjectAdapter;
import org.jacis.store.JacisStore;

/**
 * Example 2: showing stream API to access JACIS store.
 *
 * @author Jan Wiemer
 */
public class JacisExample2BasicApi {

  // Note that we use the same account object introduced for the first example

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

    // Now we show some examples how to use the stream API to access objects from the store:
    container.withLocalTx(() -> {

      // using stream methods (compute the total balance of all stored accounts):
      System.out.println("total balance=" + store.stream().mapToLong(Account::getBalance).sum());

      // using a filter (count accounts with a balance > 500):
      System.out.println("#>500=" + store.stream(acc -> acc.getBalance() > 500).count());

      // looping through the elements (adding writing an output)
      store.stream().forEach(acc -> {
        System.out.println(" - account " + acc.getName() + ": balance = " + acc.getBalance());
      });

      // output all accounts
      String str = store.stream().//
      sorted(Comparator.comparing(Account::getName)). //
      map(acc -> acc.getName() + ":" + acc.getBalance()).//
      collect(Collectors.joining(", "));
      System.out.println("Accounts: " + str);
    });

    // -------------------------------

    // Modification is lost after commit (update missing):
    container.withLocalTx(() -> {
      Account acc = store.get("account1");
      acc.deposit(1000);
    });

    // Modification succeeds (first modification then update):
    container.withLocalTx(() -> {
      Account acc = store.get("account1");
      acc.deposit(1000);
      store.update("account1", acc);
    });

    // Modification succeeds (first update then modification):
    container.withLocalTx(() -> {
      Account acc = store.get("account1");
      store.update("account1", acc);
      acc.deposit(1000);
    });

    // looping and updating (adding 10% interest to all accounts with a positive balance)
    container.withLocalTx(() -> {
      store.stream(acc -> acc.getBalance() > 0) //
          .forEach(acc -> store.update(acc.getName(), acc.deposit(acc.getBalance() / 10)));
    });

  }
}
