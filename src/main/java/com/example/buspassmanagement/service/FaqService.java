package com.example.buspassmanagement.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.buspassmanagement.model.Faq;
import com.example.buspassmanagement.repository.FaqRepository;

@Service
public class FaqService {

    @Autowired
    private FaqRepository faqRepository;

    public List<Faq> getAllFaqs() {
        return faqRepository.findAllByOrderByDisplayOrderAsc();
    }

    public Optional<Faq> getFaqById(Long id) {
        return faqRepository.findById(id);
    }

    public Faq saveFaq(Faq faq) {
        if (faq.getDisplayOrder() == null) {
            // Set display order to be last if not specified
            List<Faq> allFaqs = faqRepository.findAllByOrderByDisplayOrderAsc();
            faq.setDisplayOrder(allFaqs.isEmpty() ? 1 : allFaqs.get(allFaqs.size() - 1).getDisplayOrder() + 1);
        }
        return faqRepository.save(faq);
    }

    public void deleteFaq(Long id) {
        faqRepository.deleteById(id);
    }

    public Faq updateFaq(Long id, Faq updatedFaq) {
        return faqRepository.findById(id)
                .map(faq -> {
                    faq.setQuestion(updatedFaq.getQuestion());
                    faq.setAnswer(updatedFaq.getAnswer());
                    faq.setDisplayOrder(updatedFaq.getDisplayOrder());
                    return faqRepository.save(faq);
                })
                .orElseThrow(() -> new RuntimeException("FAQ not found with id: " + id));
    }
}
