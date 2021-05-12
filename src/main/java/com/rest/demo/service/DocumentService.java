package com.rest.demo.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hibernate.internal.util.StringHelper.isBlank;

import com.rest.demo.domain.UserPrincipal;
import com.rest.demo.domain.Document;
import com.rest.demo.domain.ServiceResponse;
import com.rest.demo.repository.DocumentRepository;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Transactional
    public ServiceResponse<Document> createDocument(UserPrincipal userPrincipal, Document doc) {
        if (userPrincipal.isHaveAuthority("ROLE_ADMIN")) {
            if (!isBlank(doc.getDocumentName()) && !isBlank(doc.getDocumentNumber()) && doc.getDocumentDate() != null) {

                // Search for document with the same number created in the same year
                List<Document> sameDocument = documentRepository.findDocument(doc.getDocumentNumber(),
                        LocalDate.of(doc.getDocumentDate().getYear(), 1, 1),
                        LocalDate.of(doc.getDocumentDate().getYear(), 12, 31));

                if (sameDocument.isEmpty())
                    return new ServiceResponse<>("success", "Document added.", documentRepository.save(doc));
                else
                    return new ServiceResponse<>("error", "Document already existed!", null);
            }
            return new ServiceResponse<>("error", "Document has empty fields!", null);
        }
        return new ServiceResponse<>("error", "User doesn't have permission for this operation!", null);
    }

    @Transactional
    public ServiceResponse<Document> editDocument(UserPrincipal userPrincipal, Document doc) {
        if (userPrincipal.isHaveAuthority("ROLE_ADMIN")) {
            if (!isBlank(doc.getDocumentName()) && !isBlank(doc.getDocumentNumber()) && doc.getDocumentDate() != null) {

                Optional<Document> editDoc = documentRepository.findById(doc.getId());

                if (editDoc.isPresent()) {

                    List<Document> sameDocument = documentRepository.findDocument(doc.getDocumentNumber(),
                            LocalDate.of(doc.getDocumentDate().getYear(), 1, 1),
                            LocalDate.of(doc.getDocumentDate().getYear(), 12, 31));

                    if (sameDocument.isEmpty()) {

                        editDoc.get().setDocumentName(doc.getDocumentName());
                        editDoc.get().setDocumentNumber(doc.getDocumentNumber());
                        editDoc.get().setDocumentDate(doc.getDocumentDate());

                        Document editedDoc = documentRepository.save(editDoc.get());

                        return new ServiceResponse<Document>("success", "Document changed.", editedDoc);
                    }
                    return new ServiceResponse<>("error", "Document already existed!", null);
                }
                return new ServiceResponse<>("error", "Document doesn't exists!", null);
            }
            return new ServiceResponse<>("error", "Document has empty fields!", null);
        }
        return new ServiceResponse<>("error", "User doesn't have permission for this operation!", null);
    }

    @Transactional
    public ServiceResponse<Document> deleteDocument(UserPrincipal userPrincipal, int docId) {
        if (userPrincipal.isHaveAuthority("ROLE_ADMIN")) {
            if (docId != 0) {

                Optional<Document> delDocument = documentRepository.findById(docId);

                if (delDocument.isPresent()) {
                    if (delDocument.get().getTaskCards().isEmpty()) {

                        documentRepository.delete(delDocument.get());
                        return new ServiceResponse<>("success", "Document successfully deleted.", delDocument.get());

                    }
                    return new ServiceResponse<>("error", "Document is linked to the task cards!", null);
                }
            }
            return new ServiceResponse<>("error", "Document not found!", null);
        }
        return new ServiceResponse<>("error", "User doesn't have permission for this operation!", null);
    }

    @Transactional
    public ServiceResponse<Page<Document>> findDocument(UserPrincipal userPrincipal, String documentName, String documentNumber, LocalDate documentDate, String documentFullName, int page, int size) {
        if (userPrincipal.isHaveAuthority("ROLE_ADMIN")) {

            Page<Document> foundDocuments = documentRepository.findDocument(documentName, documentNumber, documentDate, documentFullName, PageRequest.of(page, size));

            if (!foundDocuments.getContent().isEmpty())
                return new ServiceResponse<>("success", "Documents list.", foundDocuments);

            return new ServiceResponse<>("error", "Documents not found!", null);
        }
        return new ServiceResponse<>("error", "User doesn't have permission for this operation!", null);
    }

}
