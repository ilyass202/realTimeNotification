package com.pca.Backend.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pca.Backend.Entity.Transaction;
import com.pca.Backend.Repo.TransactionRepo;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TextController {
    private final TransactionRepo repo;
    @GetMapping("/{count}")
    public void testBenchMark(@PathVariable("count") int count){
        long start = System.currentTimeMillis();
        for(int i = 0; i<count ; i++){
            Transaction tx = new Transaction();
            tx.setUserId(Long.valueOf(i));
            tx.setDestinataireId(Long.valueOf(i+1));
            tx.setAmount(Long.valueOf((long) Math.random() * 1000));
            repo.save(tx);
        }
        long end = System.currentTimeMillis();
        double time = (start - end)/ 1000.0;
        double throu = count / time;
        double capacity = throu*86400;
    }
}
