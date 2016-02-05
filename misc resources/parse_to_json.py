def get_names(row, rows):
	if row >= len(rows):
		return []
	i = 0
	names = []
	name_buffer = ""
	while i<len(rows[row]):
		if rows[row][i]==',' or rows[row][i]=='\n':
			names.append(name_buffer)
			name_buffer = ""
		else:
			name_buffer +=rows[row][i]
		i+=1
	return names

def get_notes(row, rows):
	notes = []
	i = 0
	note_buffer = ""
	while i<len(rows[row]):
		if rows[row][i]==' ' or rows[row][i]=='\n':
			notes.append(note_buffer)
			note_buffer = ""
		else:
			note_buffer+=rows[row][i]
		i+=1
	return notes

def get_finger(row, rows):
	fingering = []
	i = 0
	fingering_buffer = ""
	while i<len(rows[row]):
		if rows[row][i]==' ' or rows[row][i]=='\n':
			fingering.append(fingering_buffer)
			fingering_buffer = ""
		else:
			fingering_buffer+=rows[row][i]
		i+=1
	return fingering

def print_chord(id,names,notes,fingerings,of):
	of.write("	\""+str(id)+"\":{\n")
	of.write("	"*2+"\"names\": {0},\n".format(names))
	#of.write("	"*identlevel+"\"modifiers\"")
	of.write("	"*2+"\"notes\": {0},\n".format(notes))
	of.write("	"*2+"\"fingerings\": {0},\n".format(fingerings))
	of.write("	},\n")

infile = open("chords.raw", "r")
outfile = open("chords_mine.json","w")
id = 0
rows = infile.readlines()
rc = 0
outfile.write("{")
while rc < len(rows):
	fingerings = []
	notes = []
	names = get_names(rc,rows)
	fingerings.append(get_finger(rc+1,rows))
	notes = get_notes(rc+2,rows)
	k = 4
	while get_names(rc, rows) == get_names(rc+k, rows) and rc < len(rows):
		fingerings.append(get_finger(rc+k+1,rows))
		k+=4
	rc +=k
	print(id,names, notes, len(fingerings))
	print_chord(id,names,notes,fingerings, outfile)
	id+=1
outfile.write("}")
