function [ pred ] = instanceMappingClassifier( dataFile )
%UNTITLED Summary of this function goes here
%   Detailed explanation goes here

clc;

C = 1;
cutthreshold = 0.8;
dataFile = '/shared/shelley/yqsong/data/20ngSim/outputMatlab/rec.autos-sci.electronics-1-new.mat';
disp('perform consine...');
predLabels = cosineClassifier(dataFile);
disp('done!');
disp('load data...');
load(dataFile);
disp('load data finished.');

% C = 10;
% cutthreshold = 0.99;
% dataFile = '/shared/shelley/yqsong/data/20ngSim/outputMatlab/rec.autos-rec.motorcycles-1-new.mat';
% % disp('perform consine...');
% % predLabels = cosineClassifier(dataFile);
% disp('perform maxMatching...');
% predLabels = maxMatchingClassifier(dataFile);
% disp('done!');
% disp('load data...');
% load(dataFile);
% disp('load data finished.');



labelNum = size(labelData, 2)
docNum = size(docData, 2)
correct = 0;
m = zeros(docNum);

% prepare label instances
lab = labelData{1};
labelName = lab{1};
labelList(1) = labelName;
labelIds = lab{2};
labelWeights = lab{3};
labelNorm = norm(labelWeights);
labelMat = lab{4};
labelMatAll = [labelMat];
labelWeightsAll = labelWeights / labelNorm;

for i = 2:labelNum
    lab = labelData{i};
    labelName = lab{1};
    labelList(i) = labelName;
    labelIds = lab{2};
    labelWeights = lab{3};
    labelNorm = norm(labelWeights);
    labelMat = lab{4};
    labelMatAll = [labelMatAll; labelMat];
    labelWeightsAll = [labelWeightsAll; labelWeights / labelNorm];
end
labelMatAllSize = size(labelMatAll, 1);
labelVecNorm = zeros(labelMatAllSize, 1);
for i = 1:labelMatAllSize
    labelVecNorm(i) = norm(labelMatAll(i,:));
end


if length(labelList) ~= 2
    Disp('Error. Should be binary classification problem.');
end

% prepare doc label mapping
mAll = zeros(docNum, labelMatAllSize);
yAll = zeros(docNum, 1);
for i = 1: docNum
    doc = docData{i};
    
    docLabel = doc{1};
    if strcmp(docLabel, labelList{1}) == 1
       yAll(i) = -1;
    end
    if strcmp(docLabel, labelList{2}) == 1
       yAll(i) = 1;
    end
    
    docIds = doc{2};
    docWeights = doc{3};
    docMat = doc{4};
    docNorm = norm(docWeights);
    docSize = size(docIds, 1);
    docVecNorm = zeros(docSize, 1);
    for j = 1:docSize
        docVecNorm(j) = norm(docMat(j,:));
    end
    
    docLabelSimMat = docMat * labelMatAll';
    docLabelNormMap = (docWeights * labelWeightsAll') ./ (docVecNorm * labelVecNorm' + 1E-20);
    docLabelSimMat = docLabelSimMat .* docLabelNormMap;
    
    plusDocLabelSimMat = +docLabelSimMat;
    mAll(i, :) =  max(plusDocLabelSimMat,[],1);
    
    if mod(i, 100) == 0
        disp(['processed ', num2str(i)]);
    end

end


for i = 1: docNum
    pred = predLabels{i};
    predScores(i) = pred{2};
end
%hist(predScores, 100);
predScoresNew = sort(predScores);
cutNum = ceil(docNum * cutthreshold);
threshold = predScoresNew(length(predScoresNew) - cutNum);

m = [];
y = [];
posNum = 0;
negNum = 0;
for i = 1: docNum
    pred = predLabels{i};
    if (pred{2} > threshold)
        m = [m; mAll(i, :)];
        if strcmp(pred{1}, labelList{1}) == 1
           y = [y; -1];
           negNum = negNum + 1;
        end
        if strcmp(pred{1}, labelList{2}) == 1
           y = [y; 1];
           posNum = posNum + 1;
        end             
    end
end

disp('Training size:');
disp(length(y));
disp('Pos number:');
disp(posNum);
disp('Neg number:');
disp(negNum);


% setup the linprog:
nrcon = size(m,2); % number of potential concepts
nrbags = length(y);
Cweights = repmat(C,nrbags,1);

% reweigh to cope with class imbalance:
Ipos = (y==+1);
Ineg = (y==-1);
Cweights(Ipos)=Cweights(Ipos)/sum(Ipos);
Cweights(Ineg)=Cweights(Ineg)/sum(Ineg);

f = [ones(2*nrcon,1)/(2*nrcon); Cweights; 0];
A = -[repmat(y,1,nrcon).*m, -repmat(y,1,nrcon).*m, speye(nrbags), y];
b = -ones(nrbags,1);
lb = [zeros(2*nrcon+nrbags,1);
			-inf];
ub = [repmat(inf,2*nrcon+nrbags+1,1)];
% now solve it:
disp('Optimizing linprog...');    
opts = optimset('display','on');
	[u,fval,exitflag,outp,lambda] = linprog(f,A,b,[],[],lb,ub,[],opts);
	
% what classifier do we have now...
v = u(1:nrcon)-u(nrcon+1:2*nrcon);
I = find(abs(v)>1e-9);
if isempty(I)
	warning('All weights are zero.');
	I = 1; 
end
W.w = v(I);
W.w0 = u(end);
% W.sva = +m(I,:);
% W.I = I;
% id = getident(a);
% W.id = id(I);
% w = prmapping(mfilename,'trained',W,getlablist(a),dim,2);
% w = setname(w,'MILES %s=%f',ktype,kpar(1));
% w = setbatch(w,0);  %NEVER use batches!!
disp('Optimization done.'); 


n = size(mAll,1);
out = zeros(n,1);
for i=1:n
	out(i,1) = mAll(i,:)*v + W.w0;
end
s_out = sign(out);
    
correct = 0;
for i = 1:n
    if s_out(i) == yAll(i)
        correct = correct + 1;
    end
end
correct/n


end

