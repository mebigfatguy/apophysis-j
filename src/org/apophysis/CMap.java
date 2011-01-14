/*

 Apophysis-j Copyright (C) 2008 Jean-Francois Bouzereau

 based on Apophysis ( http://www.apophysis.org )
 Apophysis Copyright (C) 2001-2004 Mark Townsend
 Apophysis Copyright (C) 2005-2006 Ronald Hordijk, Piotr Borys, Peter Sdobnov
 Apophysis Copyright (C) 2007 Piotr Borys, Peter Sdobnov

 based on Flam3 ( http://www.flam3.com )
 Copyright (C) 1992-2006  Scott Draves <source@flam3.com>

 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

 */

package org.apophysis;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStream;
import java.util.StringTokenizer;

class CMap {

	/*****************************************************************************/
	// CONSTANTS

	static final int RANDOMCMAP = -1;
	static final int NRCMAPS = 701;

	/*****************************************************************************/

	public static String[] cmapnames = { "south-sea-bather", "sky-flesh",
			"blue-bather", "no-name", "pillows", "mauve-splat",
			"facial-treescape", "fasion-bug", "leafy-face", "mouldy-sun",
			"sunny-harvest", "peach-tree", "fire-dragon", "ice-dragon",
			"german-landscape", "no-name", "living-mud-bomb", "cars",
			"unhealthy-tan", "daffodil", "rose", "healthy-skin", "orange",
			"white-ivy", "summer-makeup", "glow-buzz", "deep-water",
			"afternoon-beach", "dim-beach", "cloudy-brick", "burning-wood",
			"aquatic-garden", "no-name", "fall-quilt", "night-blue-sky",
			"shadow-iris", "solid-sky", "misty-field", "wooden-highlight",
			"jet-tundra", "pastel-lime", "hell", "indian-coast",
			"dentist-decor", "greenland", "purple-dress", "no-name",
			"spring-flora", "andi", "gig-o835", "rie02", "rie05", "rie11",
			"etretat.ppm", "the-hollow-needle-at-etretat.ppm",
			"rouen-cathedral-sunset.ppm", "the-houses-of-parliament.ppm",
			"starry-night.ppm", "water-lilies-sunset.ppm",
			"gogh.chambre-arles.ppm", "gogh.entrance.ppm",
			"gogh.the-night-cafe.ppm", "gogh.vegetable-montmartre.ppm",
			"matisse.bonheur-vivre.ppm", "matisse.flowers.ppm",
			"matisse.lecon-musique.ppm", "modigliani.nude-caryatid.ppm",
			"braque.instruments.ppm", "calcoast09.ppm", "dodge102.ppm",
			"ernst.anti-pope.ppm", "ernst.ubu-imperator.ppm",
			"fighting-forms.ppm", "fog25.ppm", "geyser27.ppm",
			"gris.josette.ppm", "gris.landscape-ceret.ppm",
			"kandinsky.comp-9.ppm", "kandinsky.yellow-red-blue.ppm",
			"klee.insula-dulcamara.ppm", "nile.ppm",
			"picasso.jfille-chevre.ppm", "pollock.lavender-mist.ppm",
			"yngpaint.ppm", "cl-gold-orange-green", "cl-gold-rose",
			"cl-lavender-purple-blues-black", "cl-yellow_mixed-brown-gold",
			"cl-dark_reds-white-grays", "cl-gold-dark_reds-browns-blues",
			"cl-golds-browns", "cl-purples-browns-blues-tans",
			"cl-oranges-browns-whites", "cl-blues-greens-whites",
			"cl-tans-yellows-browns", "cl-golds-browns2", "cl-pastels",
			"multi_color_1", "oranges", "multi_color_2", "rw-yellow-orange",
			"rw-multi-color-2", "rw-blue-with-red", "rw-blue-with-red-2",
			"rw-blues-3", "rw-reds-pinks-blues", "rw-browns-greens-reds-bule",
			"rw-browns-pinks-reds-blues",
			"rw-reds-greens-blues-pinks-yellows-browns",
			"rw-greens-light-to-dark", "rw-blues-reds-purples", "rw-multi-5",
			"rw-blues-black-purple", "rw-multi-colors-6",
			"rw-multi-reds-oranges", "rw-yellows-browns-goldish",
			"rw-multi-blues-with-gray", "rw-greens-multi",
			"rw-browns-orange-yellow-with-blues", "rw-reds-blues-greens-pinks",
			"rw-reds-browns-golds-tans", "dg009", "dg016", "dg031", "dg085",
			"dg086", "dg089", "Apophysis-040426-1crabgrass",
			"Apophysis-040426-12bs1fl", "Apophysis-040426-1cometnuc",
			"Apophysis-040426-1passionscross",
			"Apophysis-040426-1butterflyflower", "Apophysis-040426-1Watcher",
			"Apophysis-040426-1knotted", "Apophysis-040426-1artdeco",
			"Apophysis-040426-1expl_orange2a",
			"Apophysis-040426-1heartFlowers", "Apophysis-040426-1H-bird1g",
			"Apophysis-040426-1Emergence2", "Apophysis-040426-1Egg",
			"Apophysis-040426-1PenEgg", "Apophysis-040426-1kaosGothic",
			"Apophysis-040426-1KQNova", "Apophysis-040426-1kaosframe",
			"Apophysis-040426-147KaosRing",
			"Apophysis-040426-147Fighting_Fish",
			"Apophysis-040426-147ReachingMoon",
			"Apophysis-040426-163KaosScepter", "Apophysis-040426-163KSphere",
			"Apophysis-040426-163KInterseed", "Apophysis-040426-163XmasFlwers",
			"Apophysis-040426-163Shield", "Apophysis-040426-163AlienFlwers",
			"Apophysis-040426-163AlienFlwers4",
			"Apophysis-040426-163butterflyflwer1",
			"Apophysis-040426-163ButterflySherbert",
			"Apophysis-040426-163BFlyGate4",
			"Apophysis-040426-163BFlyGate4Inv",
			"Apophysis-040426-163CeltCross", "Apophysis-040426-163Egg4d",
			"Apophysis-040426-163FlowerFerns",
			"Apophysis-040426-163FlowerFernsInv",
			"Apophysis-040426-163FlwrFernsInv",
			"Apophysis-040426-163FloralCascade2",
			"Apophysis-040426-163FlowerBurst",
			"Apophysis-040426-163MaltesePurple",
			"Apophysis-040426-163Mycelialg", "Apophysis-040426-163MyceliaInv",
			"Apophysis-040426-163MrryGRnd", "Apophysis-040426-163SprngFlwrs",
			"Apophysis-040426-163SprngFlwersInv",
			"Apophysis-040426-163DemMask", "Apophysis-040426-163ResurectTree",
			"Apophysis-040426-163GldBlue", "Apophysis-040426-163WrldBndr",
			"Apophysis-040426-163GrnPrpl", "Apophysis-040426-163SphPart2",
			"Apophysis-040426-163StAmF", "Apophysis-040426-163StCosOwl",
			"Apophysis-040426-163StGenie", "Apophysis-040426-163St",
			"Apophysis-040426-163StSatAngel", "Apophysis-040427-1knotted",
			"Apophysis-040427-4AlngSpder", "Apophysis-040427-4AlienFlwerBwl",
			"Apophysis-040427-4AlienFlwrBwl_inv", "Apophysis-040427-4AmusePrk",
			"Apophysis-040427-4AmusePrkInv", "Apophysis-040427-4AmythIceInv",
			"Apophysis-040427-4AmythIce", "Apophysis-040427-4AngOrchid",
			"Apophysis-040427-4Leaves", "Apophysis-040427-4Bdlnds",
			"Apophysis-040427-4BnnySurp", "Apophysis-040427-4BorgEY",
			"Apophysis-040427-4BB4", "Apophysis-040427-4BflyWindw2",
			"Apophysis-040427-4BflyWndw3", "Apophysis-040427-4ChalLghtDrknss",
			"Apophysis-040427-4ChalicDrknsIce",
			"Apophysis-040427-4CactusFlwer", "Apophysis-040427-4ChrryBlssmT",
			"Apophysis-040427-4ChrryBlssm2", "Apophysis-040427-4CircAmbr",
			"Apophysis-040427-4CsmcOwl", "Apophysis-040427-4DblBeetle",
			"Apophysis-040427-4DrkMantis", "Apophysis-040427-4HolidyBull",
			"Apophysis-040427-4DrkFlorCnpy", "Apophysis-040427-4DethstrDemis",
			"Apophysis-040427-4DethstrDems", "Apophysis-040427-4DeerDemMsk",
			"Apophysis-040427-4CrouchDragn",
			"Apophysis-040427-4CopprMapleleaf",
			"Apophysis-040427-4Circulations", "Apophysis-040427-4DmnContaind",
			"Apophysis-040427-4DmnCntndWP", "Apophysis-040427-4DmnDimensn",
			"Apophysis-040427-4SatnFlorlSwag", "Apophysis-040427-4DDragHeart",
			"Apophysis-040427-4DimesPathsE", "Apophysis-040427-4DimensPathsE2",
			"Apophysis-040427-4DimensPathE2", "Apophysis-040427-4Doodles",
			"Apophysis-040427-4Doodles2", "Apophysis-040427-4doodles3",
			"Apophysis-040427-4Doodle3inv", "Apophysis-040427-6DoublEagles2",
			"Apophysis-040427-6Equinox", "Apophysis-040427-6Equinox2",
			"Apophysis-040427-6BluBrd", "Apophysis-040427-6BluBrdInv",
			"Apophysis-040427-6FaerieKng", "Apophysis-040427-6FireDemnOrch",
			"Apophysis-040427-6CsmcLottoWhl",
			"Apophysis-040427-6DreamFaeriRlm", "Apophysis-040427-6EyeUniv",
			"Apophysis-040427-6FaeriRob", "Apophysis-040427-6FaeriRob2",
			"Apophysis-040427-6FaeriRobDet", "Apophysis-040427-6FlakWhorls",
			"Apophysis-040427-11FlarCelebrat", "Apophysis-040427-11SpacTrees",
			"Apophysis-040427-11FloralQult", "Apophysis-040427-20FlwrFrnsBFly",
			"Apophysis-040427-24FracrameE", "Apophysis-040427-24FNouveau",
			"Apophysis-040427-24GuardFaeriR", "Apophysis-040427-24GoldenRays",
			"Apophysis-040427-24HunterSunset", "Apophysis-040427-25IntoWeave",
			"Apophysis-040427-26AlienMind", "Apophysis-040427-26ISpher4",
			"Apophysis-040427-26ISph2", "Apophysis-040427-26ISph11",
			"Apophysis-040427-43HeartFlwr", "Apophysis-040427-43JunglThron",
			"Apophysis-040427-44jawa", "Apophysis-040427-51KaosGrn",
			"Apophysis-040427-51KaosFish", "Apophysis-040427-51KKlown",
			"Apophysis-040427-51KaosEgg", "Apophysis-040427-51LavLace",
			"Apophysis-040427-51mudding", "Apophysis-040427-51pane;",
			"Apophysis-040427-51RiftAO", "Apophysis-040427-51ylwAlien",
			"Apophysis-040427-51elecforest", "Apophysis-040427-51ReachMoon",
			"Apophysis-040427-51satPhlox", "Apophysis-040427-51SnikRchg",
			"Apophysis-040427-51SmwhrDream", "Apophysis-040427-51eyepuzzl",
			"Apophysis-040427-51SpherInBlm",
			"Apophysis-040427-51SunrisSpacTim", "Apophysis-040427-51synaps",
			"Apophysis-040427-51StPeacocl", "Apophysis-040427-51TmplWatrs2",
			"Apophysis-040427-51TeddyScare", "Apophysis-040427-51kaosGardenr",
			"Apophysis-040427-51Thatway4", "Apophysis-040427-51ThatwayGrn",
			"Apophysis-040427-51TreeLife1", "Apophysis-040427-51TreeLife",
			"Apophysis-040427-51triflwr", "Apophysis-040427-51mitosis",
			"Apophysis-040427-51triflwer", "Apophysis-040427-51yggF",
			"Apophysis-040427-51Gwrap", "Apophysis-040428-1Gradient1",
			"Apophysis-040428-3Gradient2", "Apophysis-040602-1",
			"Apophysis-040531-100figurine_2abcd", "Apo-040627-1_chickadee_pix",
			"2u0026t.jpg", "2u0007t.jpg", "2u0010t.jpg", "2u0015t.jpg",
			"2u0017pp1t.jpg", "2u0017t.jpg", "2u0018t.jpg", "2u0020pp1t.jpg",
			"2u0020t.jpg", "2u0024t.jpg", "gradient0000.jpg", "0t0507.jpg",
			"0t0524.jpg", "0t0533.jpg", "0u0075.jpg", "0u0298.jpg",
			"0u0298pp1.jpg", "0u0303.jpg", "0u0333.jpg", "0u0752.jpg",
			"0u0768.jpg", "0u0795.jpg", "1u0214.jpg", "1u0215.jpg",
			"1u0216.jpg", "1u0216pp1.jpg", "3m0001.jpg", "3m0004.jpg",
			"3m0005.jpg", "3m0006.jpg", "3m0007.jpg", "3m0008.jpg",
			"3m0009.jpg", "3m0010.jpg", "3m0011.jpg", "3m0012.jpg",
			"3m0013.jpg", "3m0014.jpg", "3m0015.jpg", "3m0016.jpg",
			"3m0018.jpg", "4u0002.jpg", "4u0003.jpg", "4u0004.jpg",
			"4u0005.jpg", "4u0006.jpg", "4u0007.jpg", "4u0008.jpg",
			"4u0009.jpg", "4u0009b.jpg", "4u0010.jpg", "4u0011.jpg",
			"4u0012.jpg", "4u0013.jpg", "4u0019.jpg", "4u0022.jpg",
			"k2u0217.jpg", "ku0213.jpg", "ku0215.jpg", "s00026.jpg",
			"s00043.jpg", "s00118.jpg", "s00138.jpg", "s00149.jpg",
			"vchira_0001.jpg", "vchira_0003.jpg", "vchira_0012.jpg",
			"vchira_0013.jpg", "vchira_0014.jpg", "vchira_0015.jpg",
			"vchira_17.jpg", "vchira_18pp1.jpg", "vchira_19.jpg",
			"vchira_28.jpg", "vchira_2pp1.jpg", "00017", "040208-115",
			"040221-00", "040221-11", "040221-12", "040221-13", "040221-14",
			"040221-19", "040221-2", "040221-21", "040221-22", "040221-23",
			"040221-24", "040221-25", "040221-26", "040221-27", "040221-28",
			"040221-29", "040221-30", "040221-31", "040221-32", "040221-33",
			"040221-34", "040221-35", "040221-36", "040221-37", "040221-38",
			"040221-39", "040221-40", "040221-41", "040221-42", "040221-43",
			"040221-44", "040221-45", "040221-46", "040221-47", "040221-48",
			"040221-49", "040221-50", "040221-51", "040221-52", "040221-53",
			"040221-54", "040221-55", "040221-56", "040221-57", "040221-58",
			"040221-59", "040221-60", "040221-61", "040221-62", "040221-63",
			"040221-64", "040221-71", "040221-74", "040221-78", "040221-80",
			"040221-81", "040221-84", "040221-85", "040221-86", "040221-88",
			"040221-89", "040221-90", "040221-91", "040221-92", "040221-93",
			"040221-94", "040221-95", "040221-96", "040221-97", "040221-98",
			"040221-99", "040222", "040222-00", "040222-01", "040222-02",
			"040222-03", "040222-05", "040222-06", "040222-07", "040222-08",
			"040222-09", "040222-10", "040222-11", "040222-12", "040222-13",
			"040222-15", "040222-16", "040222-17", "040222-18", "040222-19",
			"040222-20", "040222-21", "040222-22", "040222-23", "040222-24",
			"040222-25", "040222-26", "040222-27", "040222-28", "040222-29",
			"040223", "040224", "040225", "040226", "040227", "040228",
			"10000", "Apophysis-040208-115d", "Apophysis-040208-115e",
			"Apophysis-040208-115g", "Apophysis-040208-115h",
			"Apophysis-040208-115i", "Apophysis-040208-115j",
			"Apophysis-040208-115k", "A_Bit_Confused", "Afternoon_Shadows",
			"Air", "Angora", "Antique", "Arizona", "Autumn_Garden",
			"Autumn_Leaves", "Autumn_Mountains", "Awakening", "Baby", "Banana",
			"Beach", "Beautiful", "Before_Dawn", "Beginning_to_Thaw", "Beige",
			"Berry_Bush", "Biology_Class", "Birthday_Party", "Bistro",
			"Blossoms", "Blue_Velvet", "Bluebells", "Blush", "Bluster",
			"Boquet_of_Roses", "Brushed_Silver", "Bubblegum", "California",
			"Canyon", "Carnations", "Carnival", "Carpenter", "Cellist",
			"Cherry", "Circus", "City_Street", "Clash", "Clouds", "Copper",
			"Coral", "Cotton_Flower", "Country_Garden", "Creamsicle",
			"Cricket_Music", "Dark_Rainbow", "Dark_Rose", "Dark_Turquoise",
			"Dark_Waters", "Darkness", "Davinci", "Daylight_Fading",
			"Dinosaurs", "Dragon", "Dust_Bunny", "Dynasty", "Easter",
			"Easter_2", "Easter_3", "Egg_Hunt", "Elements", "Embers",
			"Etomchek-040328-005", "Etomchek-040328-006",
			"Etomchek-040328-007", "Etomchek-040328-008",
			"Etomchek-040328-009", "Etomchek-040328-010",
			"Etomchek-040328-011", "Evening_Sunshine", "Evensong",
			"Exceding_Expectations", "Explosion", "Faded_Denim", "Fading_Away",
			"Fiery_Sky", "Fiesta", "First_Love", "Flame", "Flying_a_Kite",
			"Foamy_Waves", "For_Lenora", "For_Stacy", "Forest", "Frivolous",
			"Fun_Stuff", "Getting_a_Tan", "gipper", "Glade", "Glory",
			"Gold_and_Blue", "Golden", "Golden_Green", "Goldenrod", "Grape",
			"Lemon_Grass", "Magenta_and_Teal", "Mahogany", "Marina", "Meadow",
			"Mermaid", "Mesmerize", "Midnight_Wave", "Mint", "Mistic",
			"Mixed_Berry", "More_Blue", "Morning_Glories_at_Night", "Moss",
			"Moss2", "Motel_Decor", "Muddy", "Muddy_2", "Muted_Rainbow",
			"Mystery", "Neon", "Neon_Purple", "Night_Flower", "Night_Reeds",
			"No_Clue", "Nonsense", "Oak_Tree", "Ocean_Mist", "Paige", "Paris",
			"Parrot", "Pastel_Lime", "Peace", "Persia", "Persia_2", "Persia_3",
			"Pink", "Pollen", "Poppies", "Produce_Department", "Purple",
			"Queen_Anne", "Quiet", "Rainbow_Sprinkles", "Rainforest",
			"Rainy_Day_in_Spring", "Rainy_Forset", "Red_Light", "Riddle",
			"Riverside", "Rose_Bush", "Rusted", "Sachet", "Sage",
			"Saturday_Morning", "Scattered_Petals", "Sea_Mist", "Secret",
			"Serenity", "Serpent", "Sharp", "Shy_Violets", "Singe", "Slate",
			"Slightly_Messy", "Smog", "Sno_and_Shadows", "Snowy_Field",
			"Snuggle", "Soap_Bubble", "Sophia", "Strawberries", "Summer",
			"Summer_Fire", "Summer_Skies", "Summer_Tulips", "Sunbathing",
			"Sunny_Field", "Sunset", "Surfer", "Tequila", "Thistle", "Tribal",
			"Trippy", "Tropic", "True_Blue", "Tryst", "Tumbleweed",
			"Type_AB_Positive", "Underwater_Day", "Venice", "Victoria",
			"Violet", "Violet_Fog", "Watermelon", "Whisp", "Whisper",
			"Wintergrass", "Wooden", "Wooden_2", "Wooden_3", "Woodland",
			"Yellow_Silk", "Zinfandel", "040412", "040412-000", "040412-001",
			"040412-002", "040412-005", "040412-006", "040412-007",
			"040412-008", "040412-010", "040412-011", "040412-012",
			"040412-013", "040412-014", "040412-015", "040412-016" };

	/*****************************************************************************/

	static void getCMap(int index, double hue_rotation, int cmap[][]) {

		if (index == RANDOMCMAP) {
			index = (int) (Math.random() * NRCMAPS);
		}

		if ((index < 0) | (index >= NRCMAPS)) {
			index = 0;
		}

		try {
			InputStream is = Global.main.getClass().getResourceAsStream(
					"cmap.dat");

			is.skip(index * 256 * 3);

			for (int i = 0; i < 256; i++) {
				cmap[i][0] = is.read();
				cmap[i][1] = is.read();
				cmap[i][2] = is.read();
			}

			is.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	} // End of method getCMap

	/*****************************************************************************/

	static void copyPalette(int spalette[][], int dpalette[][]) {
		for (int i = 0; i < 256; i++) {
			dpalette[i][0] = spalette[i][0];
			dpalette[i][1] = spalette[i][1];
			dpalette[i][2] = spalette[i][2];
		}
	}

	/*****************************************************************************/

	static int[][] randomGradient() {
		int pal[][] = new int[256][3];
		int a, b, n, nodes;
		Color color;

		nodes = random((Global.maxNodes - 1) - (Global.minNodes - 2))
				+ (Global.minNodes - 1);
		n = 256 / nodes;
		b = 0;
		color = getRandomColor();
		pal[0][0] = color.getRed();
		pal[0][1] = color.getGreen();
		pal[0][2] = color.getBlue();

		while (true) {
			a = b;
			b = b + n;
			color = getRandomColor();
			if (b > 255) {
				b = 255;
			}
			pal[b][0] = color.getRed();
			pal[b][1] = color.getGreen();
			pal[b][2] = color.getBlue();
			RGBBlend(a, b, pal);
			if (b == 255) {
				break;
			}
		}

		return pal;

	} // End of method randomGradient

	/*****************************************************************************/

	static Color getRandomColor() {
		float hue = 0.01f * (random(Global.maxHue - (Global.minHue - 1)) + Global.minHue);
		float sat = 0.01f * (random(Global.maxSat - (Global.minSat - 1)) + Global.minSat);
		float bri = 0.01f * (random(Global.maxLum - (Global.minLum - 1)) + Global.minLum);
		return Color.getHSBColor(hue, sat, bri);
	}

	/*****************************************************************************/

	static void RGBBlend(int a, int b, int palette[][]) {
		double c, v;
		double vrange, range;

		if (a == b) {
			return;
		}

		range = b - a;

		vrange = palette[b % 256][0] - palette[a % 256][0];
		c = palette[a % 256][0];
		v = vrange / range;
		for (int i = a + 1; i <= b - 1; i++) {
			c = c + v;
			palette[i % 256][0] = (int) c;
		}

		vrange = palette[b % 256][1] - palette[a % 256][1];
		c = palette[a % 256][1];
		v = vrange / range;
		for (int i = a + 1; i <= b - 1; i++) {
			c = c + v;
			palette[i % 256][1] = (int) c;
		}

		vrange = palette[b % 256][2] - palette[a % 256][2];
		c = palette[a % 256][2];
		v = vrange / range;
		for (int i = a + 1; i <= b - 1; i++) {
			c = c + v;
			palette[i % 256][2] = (int) c;
		}

	} // End of method RGBBlend

	/*****************************************************************************/

	static String gradientFromPalette(int pal[][], String title) {
		StringBuffer sb = new StringBuffer();

		sb.append("gradient:\n");
		sb.append(" title=\"");
		sb.append(title);
		sb.append("\" smooth=no\n");

		for (int i = 0; i <= 255; i++) {
			int j = (int) Math.round(i * 399.0 / 255);
			int c = (pal[i][2] << 16) | (pal[i][1] << 8) | pal[i][0];
			sb.append(" index=" + j + " color=" + c + "\n");
		}

		sb.append("\n");

		return sb.toString();

	} // End of method gradientFromPalette

	/*****************************************************************************/

	static int[][] paletteFromGradient(BufferedReader r) {
		String line = null;
		int index[] = new int[400];
		int color[] = new int[400];

		int n = 0;

		try {
			while (true) {
				line = r.readLine();
				if (line == null) {
					break;
				}
				if (line.trim().endsWith("{")) {
					break;
				}
			}

			while (true) {
				line = r.readLine();
				if (line == null) {
					break;
				}
				if (line.indexOf("}") >= 0) {
					break;
				}
				if (line.indexOf("index=") < 0) {
					continue;
				}

				StringTokenizer tk = new StringTokenizer(line);
				if (tk.countTokens() != 2) {
					continue;
				}

				String token = tk.nextToken();
				int i = token.indexOf("=");
				if (i < 0) {
					continue;
				}
				String key = token.substring(0, i);
				String val = token.substring(i + 1);
				if (!key.equals("index")) {
					continue;
				}
				index[n] = Integer.parseInt(val);

				token = tk.nextToken();
				i = token.indexOf("=");
				if (i < 0) {
					continue;
				}
				key = token.substring(0, i);
				val = token.substring(i + 1);
				if (!key.equals("color")) {
					continue;
				}
				color[n] = Integer.parseInt(val);

				n++;
			}

		} catch (Exception ex) {
		}

		int cmap[][] = new int[256][3];

		for (int i = 0; i < n; i++) {
			int ind = index[i];
			int col = color[i];
			ind = (int) (ind * 255.0 / 399 + 0.5);
			cmap[ind][0] = (col) & 0xFF;
			cmap[ind][1] = (col >> 8) & 0xFF;
			cmap[ind][2] = (col >> 16) & 0xFF;
		}

		return cmap;

	} // End of method paletteFromGradient

	/*****************************************************************************/

	static int random(int n) {
		return (int) (Math.random() * n);
	}

	/*****************************************************************************/

} // End of class CMap

