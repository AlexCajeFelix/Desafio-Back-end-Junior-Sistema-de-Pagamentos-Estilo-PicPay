package com.example.demo.Application.UseCasesImpl;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.Application.Dtos.TransactionDto;
import com.example.demo.Core.Entities.Users;
import com.example.demo.Core.Entities.Transactions.Transactions;
import com.example.demo.Core.UseCases.TransactionRepositorysUseCase;
import com.example.demo.Core.UseCases.TransactionsUseCase;
import com.example.demo.Core.UseCases.ValidationTransactionAndOtherMethodsUseCaseImpl;


import jakarta.transaction.Transactional;

@Service
public class CreateTransactionUseCaseImpl implements TransactionsUseCase {

    @Autowired
    private TransactionRepositorysUseCase transactionSave;

    @Autowired
    private ValidationTransactionAndOtherMethodsUseCaseImpl ValidationTransactionAndOtherMethodsUseCaseImpl;

    @Transactional
    @Override
    public Transactions createTransaction(TransactionDto transactionDto) {
       
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try {
    
            Users sender = ValidationTransactionAndOtherMethodsUseCaseImpl.findByUserId(transactionDto.senderId());
            Users reciver = ValidationTransactionAndOtherMethodsUseCaseImpl.findByUserId(transactionDto.reciverId());
            ValidationTransactionAndOtherMethodsUseCaseImpl.validationTransaction(sender, transactionDto.amount(), reciver);
            
            executorService.submit(() -> {
                sender.setBalance(sender.getBalance().subtract(transactionDto.amount()));
                ValidationTransactionAndOtherMethodsUseCaseImpl.saveUser(sender);
            });

            executorService.submit(() -> {
                reciver.setBalance(reciver.getBalance().add(transactionDto.amount()));
                ValidationTransactionAndOtherMethodsUseCaseImpl.saveUser(reciver);
            });

            com.example.demo.Core.Entities.Transactions.Transactions transaction = new Transactions(null, transactionDto.amount(), sender, reciver, LocalDate.now());
            transactionSave.save(transaction);

            return transaction;

        }   finally {
            executorService.shutdownNow();
        }
    }
}
