function [ tuple ] = buildTuple( sentiments, phrases )

    if isempty(phrases)
        tuple='(2 .)';
        return;
    elseif (length(phrases)==1)
        tuple = sprintf('(%d %s)', sentiments(1), splitIfNecessary(strtrim(phrases{1})));
        return;
    else
        currentPhrase = phrases{1};
        nextPhrase = phrases{2};
        nextPhraseIndex = strfind(currentPhrase, nextPhrase);
        if (size(nextPhraseIndex,2)>1)
            nextPhraseIndex = nextPhraseIndex(1);
        end
        if (nextPhraseIndex==1)
            indices = 1:size(phrases,1);
            leftTokens = strsplit(nextPhrase,' ');
            matchedIndex = arrayfun(@(x) strcmp(x,leftTokens(end)),phrases);
            if (sum(matchedIndex) == 1)
                matchedIndex = indices(matchedIndex);
                leftTuple = buildTuple(sentiments(2:matchedIndex), phrases(2:matchedIndex));
                rightTuple = buildTuple(sentiments(matchedIndex+1:end), phrases(matchedIndex+1:end));
            else
                nextRemainingPhrase = strtrim(currentPhrase(length(nextPhrase)+1:end));
                matchedIndex = arrayfun(@(x) strcmp(x,nextRemainingPhrase),phrases);
                if (sum(matchedIndex)==1)
                    matchedIndex = indices(matchedIndex);
                    leftTuple = buildTuple(sentiments(2:matchedIndex-1), phrases(2:matchedIndex-1));
                    rightTuple = buildTuple(sentiments(matchedIndex:end), phrases(matchedIndex:end));
                else
                    leftTuple = buildTuple(sentiments(2:end), phrases(2:end));
                    rightWord = splitIfNecessary(strtrim(currentPhrase(length(nextPhrase)+1:end)));
                    rightTuple = sprintf('(%d %s)',2,rightWord);
                end
            end
        else
            if isempty(nextPhraseIndex)
                leftTuple = buildTuple(sentiments(1), phrases(1));
                rightTuple = buildTuple(sentiments(2:end), phrases(2:end));
            else
                leftWord = splitIfNecessary(strtrim(currentPhrase(1:nextPhraseIndex-1)));
                leftTuple = sprintf('(%d %s)', 2, leftWord);
                rightTuple = buildTuple(sentiments(2:end), phrases(2:end));
            end
        end        
        tuple = sprintf('(%d %s %s)',sentiments(1),leftTuple, rightTuple);
    end
end

function [ tuple ] = splitIfNecessary( phrase )
    words = strsplit(phrase,' ');
    if (length(words)==1)
        tuple = phrase;
    else
        words = flip(words);        
        tuple = sprintf('(2 %s)',words{1});
        for i=2:length(words)
            newTuple = sprintf('(2 %s)',words{i});
            tuple = sprintf('(2 %s %s)',newTuple,tuple);
        end
    end
end

