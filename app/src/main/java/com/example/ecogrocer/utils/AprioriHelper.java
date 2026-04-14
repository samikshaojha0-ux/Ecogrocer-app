package com.example.ecogrocer.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AprioriHelper {

    public static class AssociationRule {
        public Set<String> antecedent;
        public Set<String> consequent;
        public double confidence;

        public AssociationRule(Set<String> antecedent, Set<String> consequent, double confidence) {
            this.antecedent = antecedent;
            this.consequent = consequent;
            this.confidence = confidence;
        }
    }

    /**
     * Given a list of transactions (each transaction is a list of product item names/IDs),
     * this returns a list of association rules based on minimum support and confidence.
     */
    public static List<AssociationRule> generateRules(List<List<String>> transactions, double minSupportRatio, double minConfidence) {
        int minSupportCount = (int) Math.ceil(minSupportRatio * transactions.size());
        
        // 1. Find frequent 1-itemsets
        Map<Set<String>, Integer> supportCounts = new HashMap<>();
        Map<String, Integer> itemCount = new HashMap<>();
        
        for (List<String> transaction : transactions) {
            for (String item : transaction) {
                itemCount.put(item, itemCount.getOrDefault(item, 0) + 1);
            }
        }
        
        List<Set<String>> currentItemsets = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : itemCount.entrySet()) {
            if (entry.getValue() >= minSupportCount) {
                Set<String> itemset = new HashSet<>();
                itemset.add(entry.getKey());
                currentItemsets.add(itemset);
                supportCounts.put(itemset, entry.getValue());
            }
        }
        
        // 2. Find frequent k-itemsets
        int k = 2;
        Map<Set<String>, Integer> allFrequentItemsets = new HashMap<>(supportCounts);
        
        while (!currentItemsets.isEmpty()) {
            List<Set<String>> candidates = generateCandidates(currentItemsets, k);
            Map<Set<String>, Integer> candidateCounts = new HashMap<>();
            
            for (List<String> transaction : transactions) {
                Set<String> txnSet = new HashSet<>(transaction);
                for (Set<String> candidate : candidates) {
                    if (txnSet.containsAll(candidate)) {
                        candidateCounts.put(candidate, candidateCounts.getOrDefault(candidate, 0) + 1);
                    }
                }
            }
            
            currentItemsets.clear();
            for (Map.Entry<Set<String>, Integer> entry : candidateCounts.entrySet()) {
                if (entry.getValue() >= minSupportCount) {
                    currentItemsets.add(entry.getKey());
                    allFrequentItemsets.put(entry.getKey(), entry.getValue());
                }
            }
            k++;
        }
        
        // 3. Generate rules from all frequent itemsets
        List<AssociationRule> rules = new ArrayList<>();
        for (Map.Entry<Set<String>, Integer> entry : allFrequentItemsets.entrySet()) {
            Set<String> itemset = entry.getKey();
            if (itemset.size() < 2) continue;
            
            int itemsetSupport = entry.getValue();
            
            // To simplify, we generate rules with a single consequent (like [A, B] -> C)
            for (String consequentItem : itemset) {
                Set<String> antecedent = new HashSet<>(itemset);
                antecedent.remove(consequentItem);
                
                if (antecedent.isEmpty()) continue;
                
                int antecedentSupport = allFrequentItemsets.getOrDefault(antecedent, 0);
                if (antecedentSupport > 0) {
                    double confidence = (double) itemsetSupport / antecedentSupport;
                    if (confidence >= minConfidence) {
                        Set<String> consequent = new HashSet<>();
                        consequent.add(consequentItem);
                        rules.add(new AssociationRule(antecedent, consequent, confidence));
                    }
                }
            }
        }
        
        return rules;
    }

    private static List<Set<String>> generateCandidates(List<Set<String>> itemsets, int k) {
        List<Set<String>> candidates = new ArrayList<>();
        for (int i = 0; i < itemsets.size(); i++) {
            for (int j = i + 1; j < itemsets.size(); j++) {
                Set<String> union = new HashSet<>(itemsets.get(i));
                union.addAll(itemsets.get(j));
                if (union.size() == k && !candidates.contains(union)) {
                    candidates.add(union);
                }
            }
        }
        return candidates;
    }

    /**
     * Simple utility to get recommended items based on current cart items and generated rules.
     */
    public static List<String> getRecommendations(Set<String> currentCart, List<AssociationRule> generatedRules) {
        Set<String> recommendations = new HashSet<>();
        for (AssociationRule rule : generatedRules) {
            // If the antecedents are in the current cart, recommend the consequent
            if (currentCart.containsAll(rule.antecedent)) {
                for (String recommendedItem : rule.consequent) {
                    if (!currentCart.contains(recommendedItem)) {
                        recommendations.add(recommendedItem);
                    }
                }
            }
        }
        return new ArrayList<>(recommendations);
    }
}
