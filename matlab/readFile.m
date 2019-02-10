function y = readFile(filename, dimension)
% Search for number of string matches per line.  

%filename = '/shared/shelley/yqsong/data/20ngSim/outputMatlab/rec.autos-sci.electronics/rec.autos.txt';
%filename = '/shared/shelley/yqsong/data/20ngSim/outputMatlab/document-1-rec.autos-sci.electronics/1006.txt';

fid = fopen(filename);
y = 0;
tline = fgetl(fid);
[label, numLines] = strread(tline, '%s\t%d');


for i = 1:numLines
    i;
    
    
    tline1 = fgetl(fid);
    [id, weight] = strread(tline1, '%d\t%f');

    ids(i) = id;
    weights(i) = weight;
    
    spVec(id) = weight;
    
    tline2 = fgetl(fid);
    vector = strread(strtrim(tline2));
    
    d = size(vector);
    if (d ~= 0)
        matrix(i, :) = vector;
    else 
        matrix(i, :) = zeros(1, dimension);
    end
end

spVec = sparse(spVec);

y = {label, ids', weights', matrix};


fclose(fid);
end