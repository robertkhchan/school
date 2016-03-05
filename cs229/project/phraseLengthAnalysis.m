% SentenceId / PhraseId / Phrase / Sentiment
%tdfread('train.tsv');

% % Count phrase length
% phraselength = zeros(length(PhraseId),1);
% for i=1:length(PhraseId)    
%     singlePhrase = strtrim({Phrase(i,:)});
%     phraselength(i) = length(strsplit(singlePhrase{:},' '));
% end
% save('phraselength.mat','phraselength');
%

load('phraselength.mat');
load('TrainActual.csv');
load('TrainExpected.csv');
matches = TrainActual(:,2) == TrainExpected(:,2);

% Perform binning
x = phraselength;
y = matches;

topEdge = 52; % define limits
botEdge = 1; % define limits
numBins = 52; % define number of bins

binEdges = linspace(botEdge, topEdge, numBins+1);

[h,whichBin] = histc(x, binEdges);

binCount = zeros(numBins,1);
binMatched = zeros(numBins,1);
for i = 1:numBins
    flagBinMembers = (whichBin == i);
    binCount(i) = sum(flagBinMembers);
    binMatched(i) = sum(y(flagBinMembers));
end

newBinCount = zeros(8,1);
newBinCount(1:4) = binCount(1:4);
newBinCount(5) = sum(binCount(5:6));
newBinCount(6) = sum(binCount(7:8));
newBinCount(7) = sum(binCount(9:10));
newBinCount(8) = sum(binCount(11:end));

newBinMatched = zeros(8,1);
newBinMatched(1:4) = binMatched(1:4);
newBinMatched(5) = sum(binMatched(5:6));
newBinMatched(6) = sum(binMatched(7:8));
newBinMatched(7) = sum(binMatched(8:9));
newBinMatched(7) = sum(binMatched(9:10));
newBinMatched(8) = sum(binMatched(11:end));

plot(newBinMatched ./ newBinCount);
xlabel('Phrase Length');
ylabel('Percentage Accuracy');
title('Phrase Length vs. Accuracy');

