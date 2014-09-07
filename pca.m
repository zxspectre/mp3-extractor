function pca(batch15, batch16)

y=batch16';
x=batch15';
xy=[x;y];
sigma=xy'*xy;
sigma=sigma./size(xy,1);
[U,S,V]=svd(sigma);
Z=x*U(:,1:2);
Z2=y*U(:,1:2);
Zall=[Z;Z2];
palette=hsv(3);
colors=[ones(size(Z,1),3).*palette(1,:);ones(size(Z2,1),3).*palette(2,:)];
scatter(Zall(:,1),Zall(:,2),5,colors);