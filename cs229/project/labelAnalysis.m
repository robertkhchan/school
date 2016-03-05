 load('TrainActual.csv');
 load('TrainExpected.csv');
 matches = TrainActual(:,2) == TrainExpected(:,2);

labelCount = zeros(5,1);
labelMatched = zeros(5,1);
for i=0:4
    labelCount(i+1) = sum(TrainExpected(:,2) == i);
    labelMatched(i+1) = sum(TrainExpected(matches,2) == i);
end

x = 0:4;
y = [labelMatched,labelCount-labelMatched];
bar(x,y,'stacked');
legend('Matched','Mismatched')
title('Sentiment Distribution');
xlabel('Sentiment');
ylabel('Number of Phrases');