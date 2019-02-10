function y = readFile(filename)
% Search for number of string matches per line.  

%filename = '/shared/shelley/yqsong/data/20ngSim/outputMatlab/rec.autos-sci.electronics/rec.autos.txt';

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
    end
end

spVec = sparse(spVec);

y = {ids', weights', matrix};


fclose(fid);
end