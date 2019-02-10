clc;
clear;

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


lab = labelData{1};
labelName = lab{1};
labelList(1) = labelName;
labelMat = lab{4};
labelMatAll{1} = labelMat;
labelVecNorm = zeros(size(labelMat, 1), 1);
for j = 1:size(labelMat, 1)
    labelVecNorm(j) = norm(lab{4});
end
labelNorm{1} = labelVecNorm;
for i = 2:size(labelData, 2)
    lab = labelData{i};
    labelName = lab{1};
    labelList(i) = labelName;
    labelMat = lab{4};
    labelMatAll{i} = labelMat;
    labelVecNorm = zeros(size(labelMat, 1), 1);
    for j = 1:size(labelMat, 1)
        labelVecNorm(j) = norm(lab{4});
    end
    labelNorm{i} = labelVecNorm;
end

docNum = size(docData, 2)
for i = 1: docNum
    pred = predLabels{i};
    predScores(i) = pred{2};
end
%hist(predScores, 100);
predScoresNew = sort(predScores);
cutNum = ceil(docNum * cutthreshold);
threshold = predScoresNew(length(predScoresNew) - cutNum);

pairIndex = 1;
posNum = 0;
negNum = 0;
for i = 1: docNum
    if mod(i, 10) == 0
       disp(['max matching processed ', num2str(i)]);
    end
    
    pred = predLabels{i};
    if (pred{2} > threshold)

        docMat = docData{i}{4};
        docMatSize = size(docMat, 1);
        docVecNorm = zeros(docMatSize, 1);
        for j = 1:docMatSize
            docVecNorm(j) = norm(docMat(j,:));
        end

        
        if strcmp(pred{1}, labelList{1}) == 1
            
            negNum = negNum + 1;
           
            labelMat = labelMatAll{1};
            labelVecNorm = labelNorm{1};            
            docLabelSimMat = docMat * labelMat';
            docLabelNormMap = docVecNorm * labelVecNorm' + 1E-20;
            docLabelSimMat = docLabelSimMat ./ docLabelNormMap;
    
            [value, index] =  max(docLabelSimMat,[],1);
            
            colsize = size(docLabelSimMat, 2);
            for j = 1:colsize
                y{pairIndex} = 1;
                matA{pairIndex} = docMat(index(j), :);
                matB{pairIndex} = labelMat(j, :);
                pairIndex = pairIndex + 1;
            end
            
            
            labelMat = labelMatAll{2};
            labelVecNorm = labelNorm{2};            
            docLabelSimMat = docMat * labelMat';
            docLabelNormMap = docVecNorm * labelVecNorm' + 1E-20;
            docLabelSimMat = docLabelSimMat ./ docLabelNormMap;
    
            [value, index] =  max(docLabelSimMat,[],1);
            
            colsize = size(docLabelSimMat, 2);
            for j = 1:colsize
                y{pairIndex} = -1;
                matA{pairIndex} = docMat(index(j), :);
                matB{pairIndex} = labelMat(j, :);
                pairIndex = pairIndex + 1;
            end
        end
        if strcmp(pred{1}, labelList{2}) == 1
                        
            labelMat = labelMatAll{1};
            labelVecNorm = labelNorm{1};            
            docLabelSimMat = docMat * labelMat';
            docLabelNormMap = docVecNorm * labelVecNorm' + 1E-20;
            docLabelSimMat = docLabelSimMat ./ docLabelNormMap;
    
            [value, index] =  max(docLabelSimMat,[],1);
            
            colsize = size(docLabelSimMat, 2);
            for j = 1:colsize
                y{pairIndex} = -1;
                matA{pairIndex} = docMat(index(j), :);
                matB{pairIndex} = labelMat(j, :);
                pairIndex = pairIndex + 1;
           end
            
            
            labelMat = labelMatAll{2};
            labelVecNorm = labelNorm{2};            
            docLabelSimMat = docMat * labelMat';
            docLabelNormMap = docVecNorm * labelVecNorm' + 1E-20;
            docLabelSimMat = docLabelSimMat ./ docLabelNormMap;
    
            [value, index] =  max(docLabelSimMat,[],1);
            
            colsize = size(docLabelSimMat, 2);
            for j = 1:colsize
                y{pairIndex} = 1;
                matA{pairIndex} = docMat(index(j), :);
                matB{pairIndex} = labelMat(j, :);
                pairIndex = pairIndex + 1;
            end
            
        end
    end
end


%%

labelNum = size(labelData, 2)
docNum = size(docData, 2)
correct = 0;
m = zeros(docNum);

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
    docWeights = docWeights/docNorm;
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

%%

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