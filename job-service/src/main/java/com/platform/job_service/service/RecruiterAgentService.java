package com.platform.job_service.service;

import com.platform.job_service.dto.SearchIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.ai.document.Document;
import java.util.List;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruiterAgentService {

    private final ChatModel chatModel;

    public SearchIntent extractSearchIntent(String humanSentence) {
        log.info("🤖 AGENT: Analyzing human input: '{}'", humanSentence);

        BeanOutputConverter<SearchIntent> converter = new BeanOutputConverter<>(SearchIntent.class);

        String systemPrompt = """
                You are a smart assistant for a technical recruiter.
                The recruiter is going to type a natural language search query.
                Your job is to extract:
                1. 'skillsAndKeywords': Only the technical skills, job titles, or concepts they are looking for.
                2. 'exactCandidateName': The exact name of the person they are searching for (if they mentioned one).
                
                If they do not mention a specific person, leave 'exactCandidateName' as null.
                If they ONLY mention a name and no skills, leave 'skillsAndKeywords' as an empty string.
                
                Human Query: {query}
                
                {format}
                """;

        PromptTemplate template = new PromptTemplate(systemPrompt);
        Prompt prompt = template.create(Map.of(
                "query", humanSentence,
                "format", converter.getFormat()
        ));

        String rawResponse = chatModel.call(prompt).getResult().getOutput().getText();
        SearchIntent intent = converter.convert(rawResponse);

        log.info("🤖 AGENT: Extracted Intent -> Skills: [{}], Name: [{}]",
                intent.skillsAndKeywords(), intent.exactCandidateName());

        return intent;
    }



    public String synthesizeAnswer(String userQuestion, List<Document> resumes) {
        log.info("🤖 AGENT: Synthesizing final answer based on {} retrieved chunks...", resumes.size());

        if (resumes.isEmpty()) {
            return "I couldn't find any candidate profiles matching that exact description.";
        }

        StringBuilder contextBuilder = new StringBuilder();
        for (Document doc : resumes) {
            contextBuilder.append("Candidate: ").append(doc.getMetadata().get("candidateName")).append("\n");
            contextBuilder.append("Resume Extract: ").append(doc.getText()).append("\n\n");
        }

        String systemPrompt = """
                You are a helpful and professional technical recruiter assistant.
                The user asked: {question}
                
                Based STRICTLY on the following resume extracts, answer their question. 
                Keep your answer concise (2-3 sentences max). Start with a direct Yes or No if applicable.
                If the answer is not in the text, say you don't know. Do not make up information.
                
                --- RESUME EXTRACTS ---
                {context}
                """;

        PromptTemplate template = new PromptTemplate(systemPrompt);
        Prompt prompt = template.create(Map.of(
                "question", userQuestion,
                "context", contextBuilder.toString()
        ));

        return chatModel.call(prompt).getResult().getOutput().getText();
    }
}