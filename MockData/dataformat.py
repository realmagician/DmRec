fin = file('u.data', 'r')
fout = file('uMahout.data', 'w')
while True:
	line = fin.readline()
	if len(line) == 0:
		break
	strs = line.split('\t')
	if len(strs) < 3:
		continue
	lineForMahout = "%s,%s,%s\n" % (strs[0], strs[1], strs[2])
	fout.write(lineForMahout)
fin.close()
fout.close()