export enum GenderType {
	MALE = 1,
	FEMALE = 2,
	OTHER = 3,
}

export const GenderTypeLabel: { [key in GenderType]: string } = {
	[GenderType.MALE]: "Male",
	[GenderType.FEMALE]: "Female",
	[GenderType.OTHER]: "Other",
};

export const GenderTypeTitleLabel: { [key in GenderType]: string } = {
	[GenderType.MALE]: "Mr",
	[GenderType.FEMALE]: "Ms/Mrs",
	[GenderType.OTHER]: "Mx",
};
