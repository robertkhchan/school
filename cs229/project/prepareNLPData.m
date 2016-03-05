% SentenceId / PhraseId / Phrase / Sentiment
tdfread('train.tsv');

processIndex = SentenceId(1);
lastIndex = SentenceId(end);

fid=fopen('finalTrain.txt','w');
while (processIndex <= lastIndex)
    fprintf('Processing sentence %d \n',processIndex);
    
    sentiments = Sentiment(SentenceId == processIndex,:);
    phrases = {};
    processPhrases = Phrase(SentenceId == processIndex,:);
    for i=1:size(processPhrases,1)
        phrase = strtrim({processPhrases(i,:)});
        phrases = [phrases; phrase];
    end       
    output = buildTuple(sentiments, phrases);    
    output = regexprep(output,'\\','\\\\');
    fprintf(fid, output);
    fprintf(fid,'\n');
    
    processIndex = processIndex + 1;
end
fclose(fid);